package com.canteenbackend.api.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    // Mẫu HTML Email gửi từ Frontend truyền sang (chứa {{OTP}})
    private String htmlTemplate;
}