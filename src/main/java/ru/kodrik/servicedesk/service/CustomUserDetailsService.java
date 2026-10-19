package ru.kodrik.servicedesk.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.kodrik.servicedesk.entity.AppRole;
import ru.kodrik.servicedesk.entity.User;
import ru.kodrik.servicedesk.repository.UserRepository;

/**
 * Загружает пользователя из БД для аутентификации Spring Security.
 * Роли конвертируются в authority вида ROLE_XXX.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));
        log.debug("Аутентификация пользователя {}", username);
        String[] authorities = user.getRoles().stream()
                .map(AppRole::authority)
                .toArray(String[]::new);
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }
}
