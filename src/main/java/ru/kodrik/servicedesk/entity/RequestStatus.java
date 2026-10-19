package ru.kodrik.servicedesk.entity;

/**
 * Статус заявки. Русские подписи хранятся рядом с кодом,
 * чтобы не заводить отдельную таблицу-справочник статусов.
 */
public enum RequestStatus {
    NEW("Новая"),
    IN_PROGRESS("В работе"),
    COMPLETED("Завершена"),
    CANCELLED("Отменена");

    private final String label;

    RequestStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
