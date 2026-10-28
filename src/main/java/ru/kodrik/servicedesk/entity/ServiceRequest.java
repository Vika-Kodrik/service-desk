package ru.kodrik.servicedesk.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Краткое заголовок
    @Column(nullable = false, length = 200)
    private String title;

    // Описание проблемы/запроса
    @Column(nullable = false, length = 4000)
    private String description;

    // Статус исполнения
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status;

    // Приоритет
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestPriority priority;

    // Дата и время создания заявки
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Дата и время завершения
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Автор заявки
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // Категория заявки
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // Время решения в часах (для завершённых заявок)
    public Double resolutionHours() {
        if (completedAt == null) {
            return null;
        }
        return Duration.between(createdAt, completedAt).toMinutes() / 60.0;
    }

    // Нарушен ли SLA категории (только для завершённых заявок)
    public boolean isSlaViolated() {
        Double hours = resolutionHours();
        return hours != null && category != null && hours > category.getSlaHours();
    }
}
