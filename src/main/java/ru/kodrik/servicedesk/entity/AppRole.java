package ru.kodrik.servicedesk.entity;

/**
 * Роли пользователей приложения.
 *
 * <p>READ_ONLY — только просмотр данных;
 * USER — работа со своими заявками;
 * ADMIN — полный доступ и управление ролями.</p>
 *
 * <p>Хранится в БД как строка (ENUM-тип H2 не используется, чтобы схема
 * осталась переносимой), Spring Security получает роль с префиксом ROLE_.</p>
 */
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

    /** Имя authority для Spring Security, например ROLE_ADMIN. */
    public String authority() {
        return "ROLE_" + name();
    }
}
