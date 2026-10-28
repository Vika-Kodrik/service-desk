package ru.kodrik.servicedesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.kodrik.servicedesk.entity.RequestPriority;
import ru.kodrik.servicedesk.entity.RequestStatus;

@Data
public class RequestForm {

    private Long id;

    @NotBlank(message = "Укажите заголовок")
    @Size(max = 200, message = "Заголовок — до 200 символов")
    private String title;

    @NotBlank(message = "Опишите проблему")
    @Size(max = 4000, message = "Описание — до 4000 символов")
    private String description;

    @NotNull(message = "Выберите категорию")
    private Long categoryId;

    @NotNull(message = "Выберите приоритет")
    private RequestPriority priority;

    @NotNull(message = "Выберите статус")
    private RequestStatus status;
}
