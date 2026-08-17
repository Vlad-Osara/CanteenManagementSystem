package com.canteenbackend.api.order.dto;

import com.canteenbackend.api.order.model.OrderStatus;
import com.canteenbackend.api.order.model.OrderType;
import com.canteenbackend.helper.base.dto.BaseDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
public class OrderDTO extends BaseDTO {
    private UUID customerId; // LAZY
    private OrderStatus status;
    private OrderType type;
    private String note;
    private List<OrderItemDTO> orderItemDTO = new ArrayList<>();// EAGER
    private BigDecimal totalPrice;
}
