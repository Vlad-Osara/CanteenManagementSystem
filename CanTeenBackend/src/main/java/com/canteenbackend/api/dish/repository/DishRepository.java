package com.canteenbackend.api.dish.repository;

import com.canteenbackend.api.dish.model.Dish;
import com.canteenbackend.helper.base.repository.BaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class DishRepository extends BaseRepository<Dish, UUID, DishJpaRepository> {
    public DishRepository(DishJpaRepository dishJpaRepository) {
        super(dishJpaRepository, Dish.class);
    }

    public List<Dish> findByCategoryId(UUID categoryId) {

        return repository.findByCategoryId(categoryId);
    }

    public void deleteAllByCategoryId(UUID categoryId) {
        repository.deleteAllByCategoryId(categoryId);
    }

    public boolean existsByName(String name) {
        return repository.existsByName(name);
    }

    public Page<Dish> searchDishes(UUID categoryId, String search, Pageable pageable) {
        return repository.searchDishes(categoryId, search, pageable);
    }
}
