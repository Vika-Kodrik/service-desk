package ru.kodrik.servicedesk.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.kodrik.servicedesk.dto.RequestForm;
import ru.kodrik.servicedesk.entity.ServiceRequest;
import ru.kodrik.servicedesk.entity.User;
import ru.kodrik.servicedesk.service.CategoryService;
import ru.kodrik.servicedesk.service.RequestService;
import ru.kodrik.servicedesk.service.UserService;

import java.util.Comparator;
import java.util.List;

// Список заявок и форма ввода/редактирования заявки
@Controller
@RequestMapping("/requests")
@RequiredArgsConstructor
@Slf4j
public class RequestController {

    private final RequestService requestService;
    private final CategoryService categoryService;
    private final UserService userService;

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails principal, Model model) {
        User current = userService.getByUsername(principal.getUsername());

        List<ServiceRequest> requests = requestService.listFor(current);

        requests.sort(Comparator.comparing(ServiceRequest::getId));

        model.addAttribute("requests", requests);
        return "requests/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("requestForm", new RequestForm());
        model.addAttribute("categories", categoryService.findAll());
        return "requests/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails principal,
                           Model model) {
        User current = userService.getByUsername(principal.getUsername());
        ServiceRequest request = requestService.getByIdFor(id, current);
        model.addAttribute("requestForm", toForm(request));
        model.addAttribute("categories", categoryService.findAll());
        return "requests/form";
    }

    //Создание новой заявки (id пуст) или сохранение правок существующей
    @PostMapping
    public String save(@Valid @ModelAttribute("requestForm") RequestForm form,
                       BindingResult bindingResult,
                       @AuthenticationPrincipal UserDetails principal,
                       Model model) {
        User current = userService.getByUsername(principal.getUsername());
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            return "requests/form";
        }
        if (form.getId() == null) {
            requestService.create(form, current);
        } else {
            requestService.update(form.getId(), form, current);
        }
        return "redirect:/requests";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails principal) {
        User current = userService.getByUsername(principal.getUsername());
        requestService.delete(id, current);
        return "redirect:/requests";
    }

    private RequestForm toForm(ServiceRequest request) {
        RequestForm form = new RequestForm();
        form.setId(request.getId());
        form.setTitle(request.getTitle());
        form.setDescription(request.getDescription());
        form.setCategoryId(request.getCategory().getId());
        form.setPriority(request.getPriority());
        form.setStatus(request.getStatus());
        return form;
    }

    // Несуществующая заявка/категория — обратно к списку с сообщением
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleNotFound(IllegalArgumentException e) {
        log.warn("Ошибка запроса: {}", e.getMessage());
        return "redirect:/requests?error=notfound";
    }
}
