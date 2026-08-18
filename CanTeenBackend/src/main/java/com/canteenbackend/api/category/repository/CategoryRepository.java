package com.canteenbackend.api.category.repository;

import com.canteenbackend.api.category.model.Category;
import com.canteenbackend.helper.base.repository.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends BaseRepository<Category, UUID> {
    boolean existsByName(String name);
    Optional<Category> findByName(String name);
}