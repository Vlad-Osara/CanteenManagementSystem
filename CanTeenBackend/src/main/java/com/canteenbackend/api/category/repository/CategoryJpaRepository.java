package com.canteenbackend.api.category.repository;

import com.canteenbackend.api.category.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryJpaRepository extends JpaRepository<Category, UUID> {
    boolean existsByName(String name);

    Optional<Category> findByName(String name);
}
