package com.canteenbackend.api.order.mapper;

import com.canteenbackend.api.order.dto.OrderDTO;
import com.canteenbackend.api.order.dto.OrderItemDTO;
import com.canteenbackend.api.order.model.Order;
import com.canteenbackend.api.order.model.OrderItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class OrderMapper {
    public OrderDTO toOrderDTO(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .customerId(order.getCustomer().getId())
                .status(order.getStatus())
                .type(order.getType())
                .note(order.getNote())
                .orderItemDTO(order.getOrderItems() != null ? order.getOrderItems().stream().map(this::toOrderItemDTO).toList() : new ArrayList<>())
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .build();
    }

    public OrderItemDTO toOrderItemDTO(OrderItem orderItem) {
        return OrderItemDTO.builder()
                .dishId(orderItem.getDish().getId())
                .dishName(orderItem.getDish().getName())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .build();
    }
}
