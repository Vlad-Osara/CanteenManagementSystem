package com.canteenbackend.api.category.repository;

import com.canteenbackend.api.category.model.Category;
import com.canteenbackend.helper.base.repository.BaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class CategoryRepository extends BaseRepository<Category, UUID, CategoryJpaRepository> {

    @Autowired
    public CategoryRepository(CategoryJpaRepository categoryJpaRepository) {
        super(categoryJpaRepository, Category.class);
    }

    public boolean existsByName(String name) {
        return repository.existsByName(name);
    }

    public Optional<Category> findByName(String name) {
        return repository.findByName(name);
    }
}
