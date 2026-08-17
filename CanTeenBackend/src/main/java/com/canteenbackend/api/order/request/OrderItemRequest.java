package com.canteenbackend.api.order.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class OrderItemRequest {
    @NotNull(message = "Thiếu dishId của món ăn")
    private UUID dishId;

    @NotNull(message = "Thiếu số lượng món")
    @Min(value = 1, message = "Số lượng món phải lớn hơn hoặc bằng 1")
    private Integer quantity;
}
