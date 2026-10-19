package ru.kodrik.servicedesk.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.kodrik.servicedesk.entity.AppRole;
import ru.kodrik.servicedesk.entity.User;
import ru.kodrik.servicedesk.service.UserService;

import java.util.HashSet;
import java.util.Set;

/**
 * Форма управления ролями (форма 7), доступна только ADMIN (правило /admin/**).
 * Администратор назначает пользователям роли USER и ADMIN;
 * роль READ_ONLY сохраняется у всех (базовый доступ на чтение).
 */
@Controller
@RequestMapping("/admin/roles")
@RequiredArgsConstructor
@Slf4j
public class AdminRolesController {

    private final UserService userService;

    @GetMapping
    public String rolesPage(@AuthenticationPrincipal UserDetails principal, Model model) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("currentUsername", principal.getUsername());
        return "admin/roles";
    }

    @PostMapping("/{userId}")
    public String updateRoles(@PathVariable Long userId,
                              @RequestParam(required = false) boolean userRole,
                              @RequestParam(required = false) boolean adminRole,
                              @AuthenticationPrincipal UserDetails principal) {
        User target = userService.getById(userId);
        User current = userService.getByUsername(principal.getUsername());

        // Администратор не может снять роль ADMIN сам у себя — иначе он заблокирует
        // управление системой.
        if (target.getId().equals(current.getId()) && !adminRole) {
            log.warn("{} пытался снять с себя роль ADMIN", current.getUsername());
            return "redirect:/admin/roles?error=self";
        }

        Set<AppRole> roles = new HashSet<>();
        roles.add(AppRole.READ_ONLY); // базовая роль остаётся всегда
        if (userRole) {
            roles.add(AppRole.USER);
        }
        if (adminRole) {
            roles.add(AppRole.ADMIN);
        }
        userService.updateRoles(userId, roles);
        return "redirect:/admin/roles?updated=" + target.getUsername();
    }
}
