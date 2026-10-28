package ru.kodrik.servicedesk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.kodrik.servicedesk.entity.ServiceRequest;

import java.util.List;

// Доступ к заявкам. Фильтрация «только свои» выполняется этими методами на уровне БД.
//Связи category и author объявлены LAZY, а open-in-view выключен, поэтому списковые
//запросы забирают их сразу через JOIN FETCH — иначе шаблон упадёт на ленивой загрузке
//вне транзакции (LazyInitializationException).

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    @Query("""
            select r from ServiceRequest r
            join fetch r.category
            join fetch r.author
            where r.author.id = :authorId
            order by r.createdAt desc
            """)
    List<ServiceRequest> findByAuthorIdOrderByCreatedAtDesc(@Param("authorId") Long authorId);

    @Query("""
            select r from ServiceRequest r
            join fetch r.category
            join fetch r.author
            order by r.createdAt desc
            """)
    List<ServiceRequest> findAllByOrderByCreatedAtDesc();

    boolean existsByCategoryId(Long categoryId);
}
