package ru.kodrik.servicedesk.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Форма создания/редактирования категории. */
@Data
public class CategoryForm {

    /** null — создаём новую категорию, иначе редактируем существующую. */
    private Long id;

    @NotBlank(message = "Укажите название категории")
    @Size(max = 100, message = "Название — до 100 символов")
    private String name;

    @NotNull(message = "Укажите SLA в часах")
    @Min(value = 1, message = "SLA — не меньше 1 часа")
    @Max(value = 1000, message = "SLA — не больше 1000 часов")
    private Integer slaHours;

    @Size(max = 100, message = "Подразделение — до 100 символов")
    private String department;

    @Size(max = 30, message = "Телефон — до 30 символов")
    private String contactPhone;
}
