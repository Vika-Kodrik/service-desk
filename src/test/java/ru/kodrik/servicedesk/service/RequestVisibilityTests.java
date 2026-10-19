package ru.kodrik.servicedesk.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.kodrik.servicedesk.entity.ServiceRequest;
import ru.kodrik.servicedesk.entity.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Серверное разграничение доступа к заявкам (RequestService.listFor):
 * USER видит только свои заявки, ADMIN — все, «чистый» READ_ONLY — все (только просмотр).
 * Проверка выполняется на уровне сервиса, а не только в интерфейсе.
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class RequestVisibilityTests {

    @Autowired
    private RequestService requestService;

    @Autowired
    private UserService userService;

    @Test
    void userSeesOnlyOwnRequests() {
        User ivanov = userService.getByUsername("ivanov");
        List<ServiceRequest> visible = requestService.listFor(ivanov);

        assertEquals(6, visible.size(), "ivanov должен видеть только свои 6 заявок");
        // Все видимые заявки принадлежат ivanov (сравнение по id автора,
        // чтобы не провоцировать ленивую загрузку вне сессии).
        assertTrue(visible.stream().allMatch(r -> r.getAuthor().getId().equals(ivanov.getId())),
                "в списке не должно быть чужих заявок");
    }

    @Test
    void adminSeesAllRequests() {
        User admin = userService.getByUsername("admin");
        List<ServiceRequest> visible = requestService.listFor(admin);

        assertEquals(10, visible.size(), "admin должен видеть все 10 заявок");
    }

    @Test
    void readOnlyObserverSeesAllRequests() {
        User viewer = userService.getByUsername("viewer");
        List<ServiceRequest> visible = requestService.listFor(viewer);

        assertEquals(10, visible.size(), "READ_ONLY (наблюдатель) видит все заявки только для чтения");
    }
}
