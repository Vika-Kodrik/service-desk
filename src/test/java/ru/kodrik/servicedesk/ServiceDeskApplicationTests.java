package ru.kodrik.servicedesk;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

// Базовые тесты приложения: контекст запускается и вход по форме работает
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ServiceDeskApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        // Контекст Spring запускается без ошибок (БД, JPA, Security, Thymeleaf).
    }

    // Вход под учёткой admin/admin из data.sql: проверяет сквозную цепочку
    //CustomUserDetailsService -> BCrypt -> Spring Security.
    @Test
    void adminCanLogIn() throws Exception {
        mockMvc.perform(formLogin().user("admin").password("admin"))
                .andExpect(authenticated().withUsername("admin"))
                .andExpect(redirectedUrl("/requests"));
    }

    //Вход с неверным паролем отклоняется
    @Test
    void wrongPasswordIsRejected() throws Exception {
        mockMvc.perform(formLogin().user("admin").password("wrong"))
                .andExpect(unauthenticated());
    }
}
