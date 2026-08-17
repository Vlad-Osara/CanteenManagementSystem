package com.canteenbackend.api.order.request;

import com.canteenbackend.api.order.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class OrderUpdateRequest {
    @NotNull(message = "Trạng thái đơn hàng không được để trống")
    private OrderStatus status;
}
