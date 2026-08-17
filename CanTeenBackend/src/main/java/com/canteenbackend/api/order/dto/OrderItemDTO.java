package com.canteenbackend.api.order.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
public class OrderItemDTO {
    private UUID dishId;
    private String dishName;
    private int quantity;
    private BigDecimal price;
}
