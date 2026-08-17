package com.canteenbackend.api.dish.mapper;

import com.canteenbackend.api.dish.dto.DishDTO;
import com.canteenbackend.api.dish.model.Dish;
import org.springframework.stereotype.Component;

@Component
public class DishMapper {
    public DishDTO toDishDTO(Dish dish) {
        return DishDTO.builder()
                .id(dish.getId())
                .name(dish.getName())
                .description(dish.getDescription())
                .price(dish.getPrice())
                .isAvailable(dish.getIsAvailable())
                .imageUrl(dish.getImageUrl())
                .categoryName(dish.getCategory().getName())
                .categoryId(dish.getCategory().getId())
                .createdAt(dish.getCreatedAt())
                .updatedAt(dish.getUpdatedAt())
                .build();
    }
}

