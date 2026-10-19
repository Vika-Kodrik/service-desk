package ru.kodrik.servicedesk.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kodrik.servicedesk.entity.AppRole;
import ru.kodrik.servicedesk.entity.User;
import ru.kodrik.servicedesk.repository.UserRepository;

import java.util.List;
import java.util.Set;

/** Работа с пользователями: регистрация (роль READ_ONLY по умолчанию) и управление ролями. */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: id=" + id));
    }

    /** Поиск пользователя по логину — используется контроллерами для текущего пользователя. */
    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + username));
    }

    /**
     * Регистрация нового пользователя. Пароль хэшируется BCrypt,
     * новая учётная запись получает роль READ_ONLY (по заданию).
     *
     * @throws IllegalArgumentException если логин уже занят
     */
    @Transactional
    public User register(String username, String rawPassword) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Логин уже занят");
        }
        User user = new User(username, passwordEncoder.encode(rawPassword), Set.of(AppRole.READ_ONLY));
        User saved = userRepository.save(user);
        log.info("Зарегистрирован пользователь {} с ролью READ_ONLY", username);
        return saved;
    }

    /**
     * Обновление ролей пользователя (форма администратора).
     * Роль READ_ONLY сохраняется всегда — она отвечает за базовый доступ к спискам.
     */
    @Transactional
    public User updateRoles(Long userId, Set<AppRole> newRoles) {
        User user = getById(userId);
        Set<AppRole> merged = new java.util.HashSet<>(newRoles);
        merged.add(AppRole.READ_ONLY);
        user.setRoles(merged);
        log.info("Роли пользователя {} обновлены: {}", user.getUsername(), merged);
        return userRepository.save(user);
    }
}
