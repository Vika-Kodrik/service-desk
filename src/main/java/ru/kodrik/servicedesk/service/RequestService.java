package ru.kodrik.servicedesk.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kodrik.servicedesk.dto.RequestForm;
import ru.kodrik.servicedesk.entity.AppRole;
import ru.kodrik.servicedesk.entity.Category;
import ru.kodrik.servicedesk.entity.RequestStatus;
import ru.kodrik.servicedesk.entity.ServiceRequest;
import ru.kodrik.servicedesk.entity.User;
import ru.kodrik.servicedesk.repository.CategoryRepository;
import ru.kodrik.servicedesk.repository.ServiceRequestRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Логика работы с заявками. Главное правило разграничения доступа
 * выполняется именно здесь, на сервере (а не только в интерфейсе):
 *
 * <ul>
 *   <li>ADMIN — видит и редактирует все заявки;</li>
 *   <li>USER (без ADMIN) — видит и редактирует только свои заявки;</li>
 *   <li>READ_ONLY (без USER/ADMIN) — видит все заявки, но не может изменять
 *       (запись закрыта на уровне Spring Security).</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RequestService {

    private final ServiceRequestRepository requestRepository;
    private final CategoryRepository categoryRepository;

    /** Список заявок, доступных пользователю с учётом его роли. */
    @Transactional(readOnly = true)
    public List<ServiceRequest> listFor(User viewer) {
        if (seesOnlyOwn(viewer)) {
            return requestRepository.findByAuthorIdOrderByCreatedAtDesc(viewer.getId());
        }
        return requestRepository.findAllByOrderByCreatedAtDesc();
    }

    /** USER без роли ADMIN видит только свои заявки. */
    private boolean seesOnlyOwn(User viewer) {
        return viewer.hasRole(AppRole.USER) && !viewer.hasRole(AppRole.ADMIN);
    }

    /** Заявка по id с проверкой права доступа: чужую заявку USER не получит. */
    @Transactional(readOnly = true)
    public ServiceRequest getByIdFor(Long id, User viewer) {
        ServiceRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: id=" + id));
        if (seesOnlyOwn(viewer) && !request.getAuthor().getId().equals(viewer.getId())) {
            log.warn("Пользователь {} пытался открыть чужую заявку {}", viewer.getUsername(), id);
            throw new AccessDeniedException("Доступ к чужой заявке запрещён");
        }
        return request;
    }

    /** Создание заявки от имени автора. */
    @Transactional
    public ServiceRequest create(RequestForm form, User author) {
        Category category = categoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена"));
        ServiceRequest request = new ServiceRequest();
        applyForm(request, form, category);
        request.setAuthor(author);
        request.setCreatedAt(LocalDateTime.now());
        // при создании сразу в статусе «Завершена» — фиксируем время завершения
        if (request.getStatus() == RequestStatus.COMPLETED) {
            request.setCompletedAt(LocalDateTime.now());
        }
        ServiceRequest saved = requestRepository.save(request);
        log.info("Пользователь {} создал заявку №{} «{}»", author.getUsername(), saved.getId(), saved.getTitle());
        return saved;
    }

    /** Редактирование заявки с проверкой владельца. */
    @Transactional
    public ServiceRequest update(Long id, RequestForm form, User viewer) {
        ServiceRequest request = getByIdFor(id, viewer);
        Category category = categoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена"));
        applyForm(request, form, category);
        syncCompletedAt(request);
        log.info("Пользователь {} изменил заявку №{}", viewer.getUsername(), id);
        return requestRepository.save(request);
    }

    /** Удаление заявки с проверкой владельца. */
    @Transactional
    public void delete(Long id, User viewer) {
        ServiceRequest request = getByIdFor(id, viewer);
        requestRepository.delete(request);
        log.info("Пользователь {} удалил заявку №{}", viewer.getUsername(), id);
    }

    private void applyForm(ServiceRequest request, RequestForm form, Category category) {
        request.setTitle(form.getTitle());
        request.setDescription(form.getDescription());
        request.setPriority(form.getPriority());
        request.setStatus(form.getStatus());
        request.setCategory(category);
    }

    /**
     * Время завершения синхронно со статусом: «Завершена» — фиксируем момент
     * (если ещё не зафиксирован), другой статус — сбрасываем.
     */
    private void syncCompletedAt(ServiceRequest request) {
        if (request.getStatus() == RequestStatus.COMPLETED) {
            if (request.getCompletedAt() == null) {
                request.setCompletedAt(LocalDateTime.now());
            }
        } else {
            request.setCompletedAt(null);
        }
    }
}
