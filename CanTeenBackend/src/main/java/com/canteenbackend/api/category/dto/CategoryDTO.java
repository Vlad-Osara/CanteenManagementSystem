package com.canteenbackend.api.category.dto;

import com.canteenbackend.helper.base.dto.BaseDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class CategoryDTO extends BaseDTO {
    private String name;
    private String description;
}
