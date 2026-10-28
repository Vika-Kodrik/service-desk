package ru.kodrik.servicedesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrationForm {

    @NotBlank(message = "Укажите логин")
    @Size(min = 3, max = 50, message = "Логин — от 3 до 50 символов")
    private String username;

    @NotBlank(message = "Укажите пароль")
    @Size(min = 4, max = 100, message = "Пароль — от 4 до 100 символов")
    private String password;

    @NotBlank(message = "Подтвердите пароль")
    private String confirmPassword;
}
