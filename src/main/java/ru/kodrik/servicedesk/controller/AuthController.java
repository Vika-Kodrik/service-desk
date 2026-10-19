package ru.kodrik.servicedesk.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.kodrik.servicedesk.dto.RegistrationForm;
import ru.kodrik.servicedesk.service.UserService;

/** Вход в систему (форма 1) и регистрация новых пользователей. */
@Controller
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    /** Форма входа обрабатывается Spring Security, здесь только представление. */
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "register";
    }

    /**
     * Регистрация: новая учётная запись получает роль READ_ONLY.
     * Ошибки валидации и занятый логин возвращают пользователя к форме.
     */
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                           BindingResult bindingResult,
                           Model model) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "Пароли не совпадают");
        }
        if (bindingResult.hasErrors()) {
            return "register";
        }
        try {
            userService.register(form.getUsername(), form.getPassword());
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("username", "occupied", e.getMessage());
            return "register";
        }
        return "redirect:/login?registered";
    }
}
