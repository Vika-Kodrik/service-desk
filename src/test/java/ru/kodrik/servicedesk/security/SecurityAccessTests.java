package ru.kodrik.servicedesk.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Проверка правил доступа из WebSecurityCon/
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityAccessTests {

    @Autowired
    private MockMvc mockMvc;

    // Не аутентифицированные запросы

    @Test
    void anonymousIsRedirectedToLogin() throws Exception {
        mockMvc.perform(get("/requests"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void adminPageRequiresAdminRole() throws Exception {
        mockMvc.perform(get("/admin/roles"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void actuatorHealthIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void actuatorInfoIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app.name").exists());
    }

    // USER

    @Test
    @WithMockUser(username = "ivanov", roles = "USER")
    void userCannotOpenAdminPage() throws Exception {
        // accessDeniedPage у аутентифицированного без прав: Spring Security
        // отвечает 403 и делает forward (внутреннюю переадресацию) на страницу
        // «Доступ запрещён» — редиректа здесь нет.
        mockMvc.perform(get("/admin/roles"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/access-denied"));
    }

    @Test
    @WithMockUser(username = "ivanov", roles = "USER")
    void userCanOpenRequestsAndStats() throws Exception {
        mockMvc.perform(get("/requests"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/stats"))
                .andExpect(status().isOk());
    }

    // READ_ONLY

    @Test
    @WithMockUser(username = "viewer", roles = "READ_ONLY")
    void readOnlyCanViewButNotCreateOrSeeStats() throws Exception {
        // Просмотр списков доступен, создание заявок и статистика — нет:
        // 403 + forward на страницу «Доступ запрещён» (см. тест выше).
        mockMvc.perform(get("/requests"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/requests/new"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/access-denied"));
        mockMvc.perform(get("/stats"))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/access-denied"));
    }
}
