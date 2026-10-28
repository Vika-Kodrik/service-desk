package ru.kodrik.servicedesk.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kodrik.servicedesk.dto.StatsView;
import ru.kodrik.servicedesk.entity.AppRole;
import ru.kodrik.servicedesk.entity.RequestPriority;
import ru.kodrik.servicedesk.entity.RequestStatus;
import ru.kodrik.servicedesk.entity.ServiceRequest;
import ru.kodrik.servicedesk.entity.User;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Расчёт статистики
@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {

    private final RequestService requestService;

    @Transactional(readOnly = true)
    public StatsView computeFor(User viewer) {
        List<ServiceRequest> requests = requestService.listFor(viewer);
        String scope = viewer.hasRole(AppRole.ADMIN)
                ? "по всем заявкам"
                : "по вашим заявкам";

        // Количество по статусам (все значения перечисления, включая нулевые)
        Map<RequestStatus, Long> byStatus = new EnumMap<>(RequestStatus.class);
        for (RequestStatus status : RequestStatus.values()) {
            byStatus.put(status, 0L);
        }
        requests.forEach(r -> byStatus.merge(r.getStatus(), 1L, Long::sum));

        // Количество по приоритетам
        Map<RequestPriority, Long> byPriority = new EnumMap<>(RequestPriority.class);
        for (RequestPriority priority : RequestPriority.values()) {
            byPriority.put(priority, 0L);
        }
        requests.forEach(r -> byPriority.merge(r.getPriority(), 1L, Long::sum));

        // Распределение по категориям (по убыванию количества)
        Map<String, Long> byCategory = new LinkedHashMap<>();
        requests.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        r -> r.getCategory().getName(), java.util.stream.Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .forEach(e -> byCategory.put(e.getKey(), e.getValue()));

        // Среднее время решения завершённых заявок
        List<ServiceRequest> completed = requests.stream()
                .filter(r -> r.getStatus() == RequestStatus.COMPLETED && r.resolutionHours() != null)
                .toList();
        Double avgHours = completed.isEmpty() ? null
                : completed.stream().mapToDouble(ServiceRequest::resolutionHours).average().orElse(0);

        // Нарушения SLA:
        long slaViolated = completed.stream().filter(ServiceRequest::isSlaViolated).count();
        double slaShare = completed.isEmpty() ? 0
                : 100.0 * slaViolated / completed.size();

        log.debug("Статистика для {}: всего={}, завершено={}, SLA нарушено={}",
                viewer.getUsername(), requests.size(), completed.size(), slaViolated);

        return new StatsView(
                requests.size(),
                byStatus,
                byPriority,
                byCategory,
                completed.size(),
                avgHours == null ? null : Math.round(avgHours * 10) / 10.0,
                slaViolated,
                Math.round(slaShare * 10) / 10.0,
                scope);
    }
}
