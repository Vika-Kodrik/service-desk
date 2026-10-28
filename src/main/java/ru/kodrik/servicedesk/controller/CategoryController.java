package ru.kodrik.servicedesk.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.kodrik.servicedesk.dto.CategoryForm;
import ru.kodrik.servicedesk.entity.Category;
import ru.kodrik.servicedesk.service.CategoryService;

import java.util.Comparator;
import java.util.List;

//Список категорий и форма ввода/редактирования категории
//Изменение справочника доступно только ADMIN — правила заданы в WebSecurityConfig.
@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String list(Model model) {
        List<Category> categories = categoryService.findAll();
        categories.sort(Comparator.comparing(Category::getId)); // Сортируем по ID
        model.addAttribute("categories", categories);
        return "categories/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("categoryForm", new CategoryForm());
        return "categories/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Category category = categoryService.getById(id);
        model.addAttribute("categoryForm", toForm(category));
        return "categories/form";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute("categoryForm") CategoryForm form,
                       BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "categories/form";
        }
        try {
            if (form.getId() == null) {
                categoryService.create(form);
            } else {
                categoryService.update(form.getId(), form);
            }
        } catch (IllegalStateException e) {
            bindingResult.rejectValue("name", "duplicate", e.getMessage());
            return "categories/form";
        }
        return "redirect:/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        categoryService.delete(id);
        return "redirect:/categories";
    }

    private CategoryForm toForm(Category category) {
        CategoryForm form = new CategoryForm();
        form.setId(category.getId());
        form.setName(category.getName());
        form.setSlaHours(category.getSlaHours());
        form.setDepartment(category.getDepartment());
        form.setContactPhone(category.getContactPhone());
        return form;
    }

    // Попытка удалить категорию с заявками или несуществующую категорию
    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public String handleConflict(RuntimeException e) {
        log.warn("Операция с категорией отклонена: {}", e.getMessage());
        return "redirect:/categories?error=" + (e instanceof IllegalStateException ? "inuse" : "notfound");
    }
}
