package ru.kodrik.servicedesk.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Категория заявки — справочная таблица (categories).
 *
 * <p>Для каждой категории задан норматив времени реакции/решения SLA в часах:
 * заявка считается просроченной, если решена дольше, чем slaHours.</p>
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Название категории, например «Проблема с ПК», «Доступ». */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /** Норматив SLA на решение заявки этой категории, часов. */
    @Column(name = "sla_hours", nullable = false)
    private int slaHours;

    /** Ответственное подразделение (владелец категории), необязательное. */
    @Column(length = 100)
    private String department;

    /** Контактный телефон поддержки по категории, необязательный. */
    @Column(length = 30)
    private String contactPhone;
}
