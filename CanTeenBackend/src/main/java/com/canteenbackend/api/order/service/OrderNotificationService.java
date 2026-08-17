package com.canteenbackend.api.order.service;

import com.canteenbackend.api.order.dto.OrderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    // Notify staff about new order
    public void notifyStaffNewOrder(OrderDTO orderDTO) {
        messagingTemplate.convertAndSend("/topic/staff/orders", orderDTO);
    }

    // Notify staff about order update
    public void notifyStaffOrderUpdated(OrderDTO orderDTO) {
        messagingTemplate.convertAndSend("/topic/staff/orders/updated", orderDTO);
    }

    // Notify customer about order update
    public void notifyCustomerOrderUpdated(UUID customerId, OrderDTO orderDTO) {
        messagingTemplate.convertAndSend("/topic/customer/" + customerId + "/orders", orderDTO);
    }

    public void notifyDishAvailability(com.canteenbackend.api.dish.dto.DishDTO dishDTO) {
        messagingTemplate.convertAndSend("/topic/dishes/availability", dishDTO);
    }
}