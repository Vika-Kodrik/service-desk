package ru.kodrik.servicedesk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Публичные страницы и ресурсы
                        .requestMatchers("/", "/about", "/login", "/register").permitAll()
                        .requestMatchers("/css/**", "/js/**").permitAll()
                        // Actuator:  health и info
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // Консоль H2 только для локальной отладки
                        .requestMatchers("/h2-console/**").permitAll()
                        // Управление ролями только администратор
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Статистика USER и выше (READ_ONLY без USER не пускаем)
                        .requestMatchers("/stats").hasAnyRole("USER", "ADMIN")
                        // Изменение данных: заявки — USER и ADMIN,
                        // справочник категорий — только ADMIN
                        .requestMatchers("/requests/new", "/requests/*/edit", "/requests/*/delete")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/categories/new", "/categories/*/edit", "/categories/*/delete")
                        .hasRole("ADMIN")
                        // Всё остальное (списки, просмотр) — любой аутентифицированный
                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/requests", false)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"))
                // Консоль H2 работает во фрейме
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                // Консоль H2 не поддерживает CSRF-токены
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));
        return http.build();
    }
}
