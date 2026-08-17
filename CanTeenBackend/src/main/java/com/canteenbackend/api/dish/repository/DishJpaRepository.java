package com.canteenbackend.api.dish.repository;

import com.canteenbackend.api.dish.model.Dish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DishJpaRepository extends JpaRepository<Dish, UUID> {
    List<Dish> findByCategoryId(UUID categoryId);

    void deleteAllByCategoryId(UUID categoryId);

    boolean existsByName(String name);

    @Query("SELECT d FROM Dish d WHERE " +
            "(:categoryId IS NULL OR d.category.id = :categoryId) AND " +
            "(:search IS NULL OR :search = '' OR LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Dish> searchDishes(@Param("categoryId") UUID categoryId,
                            @Param("search") String search,
                            Pageable pageable);
}
