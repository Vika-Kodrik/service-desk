package ru.kodrik.servicedesk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kodrik.servicedesk.entity.User;

import java.util.Optional;

/** Доступ к пользователям. */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
