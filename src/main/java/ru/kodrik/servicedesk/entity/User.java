package ru.kodrik.servicedesk.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Пользователь приложения (таблица users).
 *
 * <p>Пароль хранится только в виде BCrypt-хэша. Роли — перечисление
 * {@link AppRole}, сохраняемое в отдельной таблице user_roles
 * (один пользователь может иметь несколько ролей).</p>
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Логин, уникальный, используется для входа. */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** BCrypt-хэш пароля (открытый текст не хранится и не принимается). */
    @Column(nullable = false)
    private String password;

    /** Роли пользователя; EAGER — нужны при каждой аутентификации. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Set<AppRole> roles = new HashSet<>();

    public User(String username, String password, Set<AppRole> roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
    }

    public boolean hasRole(AppRole role) {
        return roles != null && roles.contains(role);
    }
}
