package com.canteenbackend.api.auth;

import com.canteenbackend.api.auth.request.ForgotPasswordRequest;
import com.canteenbackend.api.auth.request.LoginRequest;
import com.canteenbackend.api.auth.request.RegisterRequest;
import com.canteenbackend.api.auth.request.ResetPasswordRequest;
import com.canteenbackend.api.auth.service.AuthService;
import com.canteenbackend.api.user.service.UserService;
import com.canteenbackend.helper.base.response.MessageResponse;
import com.canteenbackend.helper.base.response.ResponseObject;
import com.canteenbackend.utils.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final SecurityUtils securityUtils;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest httpRequest) {
        try {
            return ResponseEntity.ok(authService.login(loginRequest, httpRequest));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Đăng nhập thất bại: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        return ResponseEntity.ok(ResponseObject.success("Lấy thông tin người dùng đăng nhập thành công", userService.get(securityUtils.getCurrentUserDetails().getId())));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            authService.forgotPassword(request);
            return ResponseEntity.ok(new MessageResponse("Mã OTP đã được gửi thành công đến email của bạn!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Gửi mã thất bại: " + e.getMessage()));
        }
    }
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request);
            return ResponseEntity.ok(new MessageResponse("Đặt lại mật khẩu thành công! Bạn có thể đăng nhập bằng mật khẩu mới."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Đặt lại mật khẩu thất bại: " + e.getMessage()));
        }
    }
}
