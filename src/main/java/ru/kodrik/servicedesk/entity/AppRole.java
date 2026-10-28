package ru.kodrik.servicedesk.entity;

public enum AppRole {
    READ_ONLY("Только просмотр"),
    USER("Пользователь"),
    ADMIN("Администратор");

    private final String label;

    AppRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String authority() {
        return "ROLE_" + name();
    }
}
