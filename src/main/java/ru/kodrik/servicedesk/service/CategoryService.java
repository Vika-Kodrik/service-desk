package ru.kodrik.servicedesk.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kodrik.servicedesk.dto.CategoryForm;
import ru.kodrik.servicedesk.entity.Category;
import ru.kodrik.servicedesk.repository.CategoryRepository;

import java.util.List;

/** Работа со справочником категорий (создание/правка — только ADMIN на уровне Security). */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAllByOrderByName();
    }

    @Transactional(readOnly = true)
    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: id=" + id));
    }

    @Transactional
    public Category create(CategoryForm form) {
        checkNameFree(form.getName(), null);
        Category category = new Category();
        applyForm(category, form);
        Category saved = categoryRepository.save(category);
        log.info("Создана категория «{}» (SLA {} ч)", saved.getName(), saved.getSlaHours());
        return saved;
    }

    @Transactional
    public Category update(Long id, CategoryForm form) {
        Category category = getById(id);
        checkNameFree(form.getName(), id);
        applyForm(category, form);
        log.info("Изменена категория id={} «{}»", id, category.getName());
        return categoryRepository.save(category);
    }

    /**
     * Удаление категории запрещено, если по ней есть заявки:
     * иначе заявки останутся без категории и SLA.
     */
    @Transactional
    public void delete(Long id) {
        Category category = getById(id);
        if (categoryRepository.existsById(id)) {
            throw new IllegalStateException(
                    "Нельзя удалить категорию «" + category.getName() + "»: по ней есть заявки");
        }
        categoryRepository.delete(category);
        log.info("Удалена категория id={}", id);
    }

    private void applyForm(Category category, CategoryForm form) {
        category.setName(form.getName());
        category.setSlaHours(form.getSlaHours());
        category.setDepartment(form.getDepartment());
        category.setContactPhone(form.getContactPhone());
    }

    private void checkNameFree(String name, Long excludeId) {
        categoryRepository.findAll().stream()
                .filter(c -> excludeId == null || !c.getId().equals(excludeId))
                .forEach(c -> {
                    if (c.getName().equalsIgnoreCase(name)) {
                        throw new IllegalStateException("Категория с таким названием уже существует");
                    }
                });
    }
}
