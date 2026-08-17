package com.canteenbackend.api.category.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryStoreRequest {
    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;
    private String description;
}
