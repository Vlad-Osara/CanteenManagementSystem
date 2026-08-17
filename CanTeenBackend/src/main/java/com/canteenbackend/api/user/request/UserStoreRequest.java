package com.canteenbackend.api.user.request;

import com.canteenbackend.helper.base.model.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserStoreRequest {
    @NotBlank(message = "Tên là bắt buộc")
    @Size(min = 10, max = 100, message = "Tên không được vượt quá 100 ký tự à ít nhất 10 kí tự")
    private String fullName;

    @NotBlank(message = "Email là bắt buộc")
    @Email(message = "Email phải đúng định dạng")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[35789]\\d{8}$",
            message = "Số điện thoại không đúng định dạng Việt Nam (phải gồm 10 số và bắt đầu bằng 03, 05, 07, 08 hoặc 09)"
    )
    private String phoneNumber;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 20, message = "Tên đăng nhập phải từ 3 đến 20 ký tự")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z]).{8,}$",
            message = "Mật khẩu phải có ít nhất 8 ký tự, bao gồm cả chữ và số")
    private String password;

    @NotNull(message = "Chức vụ không được để trống")
    private Role role;
}
