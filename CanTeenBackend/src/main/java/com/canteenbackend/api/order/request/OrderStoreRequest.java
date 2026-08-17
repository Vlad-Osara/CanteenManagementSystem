package com.canteenbackend.api.order.request;

import com.canteenbackend.api.order.model.OrderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class OrderStoreRequest {
    private String note;

    @NotNull(message = "Phải có loại là DINE_IN hoặc TAKE_AWAY")
    private OrderType type;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(
            regexp = "^$|^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
            message = "Mật khẩu phải có ít nhất 8 ký tự, bao gồm cả chữ và số"
    )
    private String confirmPassword;

    @NotEmpty(message = "Đơn hàng phải có ít nhất một sản phẩm")
    @Valid
    private List<OrderItemRequest> items;
}
