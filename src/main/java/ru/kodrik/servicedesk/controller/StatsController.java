package ru.kodrik.servicedesk.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.kodrik.servicedesk.entity.User;
import ru.kodrik.servicedesk.service.StatsService;
import ru.kodrik.servicedesk.service.UserService;

/** Форма статистики (форма 6). Доступ: USER и ADMIN (правило в WebSecurityConfig). */
@Controller
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;
    private final UserService userService;

    @GetMapping
    public String stats(@AuthenticationPrincipal UserDetails principal, Model model) {
        User current = userService.getByUsername(principal.getUsername());
        model.addAttribute("stats", statsService.computeFor(current));
        return "stats";
    }
}
