package ru.kodrik.servicedesk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kodrik.servicedesk.entity.Category;

import java.util.List;

//Доступ к справочнику категорий
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByOrderByName();

    boolean existsByNameIgnoreCase(String name);
}
