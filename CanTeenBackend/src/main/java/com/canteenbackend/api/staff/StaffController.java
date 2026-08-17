package com.canteenbackend.api.staff;

import com.canteenbackend.api.dish.service.DishService;
import com.canteenbackend.api.order.dto.OrderDTO;
import com.canteenbackend.api.order.request.OrderGetRequest;
import com.canteenbackend.api.order.request.OrderUpdateRequest;
import com.canteenbackend.api.order.service.OrderNotificationService;
import com.canteenbackend.api.order.service.OrderService;
import com.canteenbackend.helper.base.response.ResponseObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {
    private final OrderService orderService;
    private final DishService dishService;
    private final OrderNotificationService orderNotificationService;

    @GetMapping("/order/active")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> getActiveOrder(@ModelAttribute OrderGetRequest orderGetRequest){
        return ResponseEntity.ok(ResponseObject.success("Lấy danh sách đơn hiện đang xử lý thành công", orderService.getActiveOrder(orderGetRequest)));
    }

    @PutMapping("/order/{id}/status")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> updateOrderStatus(@PathVariable UUID id, @Validated @RequestBody OrderUpdateRequest orderUpdateRequest) {
        OrderDTO updatedOrder = orderService.update(id, orderUpdateRequest);
        orderNotificationService.notifyStaffOrderUpdated(updatedOrder);
        return ResponseEntity.ok(ResponseObject.success("Cap nhat trang thai thanh cong", updatedOrder));
    }

    @PutMapping("/dishes/{id}/availability")
    public ResponseEntity<?> toggleDishAvailability(@PathVariable UUID id) {
        return ResponseEntity.ok(ResponseObject.success("Cap nhat trang thai thanh cong", dishService.toggleAvailability(id)));
    }
}
