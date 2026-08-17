package com.canteenbackend.api.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 20, message = "Tên đăng nhập phải từ 3 đến 20 ký tự")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z]).{8,}$",
            message = "Mật khẩu phải có ít nhất 8 ký tự, bao gồm cả chữ và số")
    private String password;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 10, max = 100, message = "Tên không được vượt quá 100 ký tự và ít nhất 10 kí tự")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[35789]\\d{8}$",
            message = "Số điện thoại không đúng định dạng Việt Nam (phải gồm 10 số và bắt đầu bằng 03, 05, 07, 08 hoặc 09)"
    )
    private String phoneNumber;
}
