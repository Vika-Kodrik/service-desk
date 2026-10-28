package ru.kodrik.servicedesk.entity;

public enum RequestPriority {
    LOW("Низкий"),
    MEDIUM("Средний"),
    HIGH("Высокий"),
    CRITICAL("Критический");

    private final String label;

    RequestPriority(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
