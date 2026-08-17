package com.canteenbackend.api.dish.dto;

import com.canteenbackend.helper.base.dto.BaseDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
@SuperBuilder
public class DishDTO extends BaseDTO {
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean isAvailable;
    private String categoryName;
    private UUID categoryId;
    private String imageUrl;
}
