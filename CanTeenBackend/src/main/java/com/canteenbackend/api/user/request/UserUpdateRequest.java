package com.canteenbackend.api.user.request;

import com.canteenbackend.helper.base.model.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserUpdateRequest {
    @Size(min = 10, max = 100, message = "Tên không được vượt quá 100 ký tự và ít nhất 10 kí tự")
    @Pattern(regexp = "^(?!\\s*$).+", message = "Tên không được chỉ chứa khoảng trắng")
    private String fullName;

    @Email(message = "Email phải đúng định dạng")
    @Size(max = 150, message = "Email không được vượt quá 150 ký tự")
    private String email;

    @Pattern(
            regexp = "^$|^(03|05|07|08|09)[0-9]{8}$",
            message = "Số điện thoại không đúng định dạng Việt Nam (phải gồm 10 số và bắt đầu bằng 03, 05, 07, 08 hoặc 09)"
    )
    private String phoneNumber;

    @DecimalMin(value = "0.0", inclusive = true, message = "Số dư không được nhỏ hơn 0đ")
    @Digits(integer = 9, fraction = 2, message = "Số dư không hợp lệ (tối đa 9 chữ số phần nguyên và 2 chữ số thập phân)")
    private BigDecimal balance;

    @Pattern(
            regexp = "^$|^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
            message = "Mật khẩu phải có ít nhất 8 ký tự, bao gồm cả chữ và số"
    )
    @NotBlank(message = "Vui lòng nhập mật khẩu hiện tại của bạn để xác nhận thay đổi")
    private String confirmPassword;

    @Pattern(
            regexp = "^$|^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
            message = "Mật khẩu phải có ít nhất 8 ký tự, bao gồm cả chữ và số"
    )
    private String newPassword;

    private Role role;
}
