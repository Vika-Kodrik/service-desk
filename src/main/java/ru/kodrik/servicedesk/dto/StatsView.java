package ru.kodrik.servicedesk.dto;

import ru.kodrik.servicedesk.entity.RequestPriority;
import ru.kodrik.servicedesk.entity.RequestStatus;

import java.util.Map;

/**
 * Готовые данные для формы статистики (форма 6).
 * Значения уже посчитаны сервисом, шаблон только выводит их.
 */
public record StatsView(
        int total,
        Map<RequestStatus, Long> byStatus,
        Map<RequestPriority, Long> byPriority,
        Map<String, Long> byCategory,
        long completedCount,
        Double avgCompletionHours,
        long slaViolatedCount,
        double slaViolatedSharePercent,
        String scope) {
}
