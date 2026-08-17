package com.canteenbackend.api.dish.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class DishStoreRequest {
    @NotBlank(message = "Tên món ănn không được để trống")
    private String name;

    private String description;

    @NotNull(message = "Giá món ăn không được để trống")
    private BigDecimal price;

    @NotNull(message = "Phải chọn danh mục của món ăn")
    private UUID categoryId;

    @NotBlank(message = "Ảnh minh họa của món ăn không được để trống")
    private String imageUrl;

    @NotNull(message = "Hãy thiết lập trạng thái có sẵn của món ăn")
    private Boolean isAvailable;
}
