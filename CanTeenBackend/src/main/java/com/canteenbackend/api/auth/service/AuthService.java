package com.canteenbackend.api.auth.service;

import com.canteenbackend.api.auth.model.PasswordResetToken;
import com.canteenbackend.api.auth.repository.PasswordResetTokenRepository;
import com.canteenbackend.api.auth.request.ForgotPasswordRequest;
import com.canteenbackend.api.auth.request.LoginRequest;
import com.canteenbackend.api.auth.request.RegisterRequest;
import com.canteenbackend.api.auth.request.ResetPasswordRequest;
import com.canteenbackend.api.user.dto.UserDTO;
import com.canteenbackend.api.user.mapper.UserMapper;
import com.canteenbackend.api.user.model.User;
import com.canteenbackend.api.user.repository.UserRepository;
import com.canteenbackend.exceptions.custom.BadRequestException; // Đổi sang Exception custom của bạn
import com.canteenbackend.exceptions.custom.ResourceNotFoundException;
import com.canteenbackend.helper.base.model.Role;
import com.canteenbackend.utils.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    public UserDTO login(LoginRequest loginRequest, HttpServletRequest httpRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new BadRequestException("Tên tài khoản hoặc mật khẩu không chính xác!"));
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new BadRequestException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Quản trị viên!");
        }
        // 1. Xác thực tài khoản (Spring Security sẽ tự gọi CustomUserDetailsService ngầm)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        // 2. Lưu vào Security Context và Session
        SecurityContextHolder.getContext().setAuthentication(authentication);
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        return userMapper.toUserDTO(user);
    }

    @Transactional
    public UserDTO register(RegisterRequest registerRequest) {
        // Thay vì ném RuntimeException (ra lỗi 500), dùng BadRequestException (ra lỗi 400) đã cấu hình ở GlobalExceptionHandler
        List<User> existingUsers = userRepository.findExistingUsers(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                registerRequest.getPhoneNumber()
        );

        Map<String, String> duplicateErrors = new HashMap<>();

        for (User existingUser : existingUsers) {
            if (existingUser.getUsername().equalsIgnoreCase(registerRequest.getUsername())) {
                duplicateErrors.put("username", "Tên tài khoản đã được sử dụng!");
            }
            if (existingUser.getEmail().equalsIgnoreCase(registerRequest.getEmail())) {
                duplicateErrors.put("email", "Email đã được sử dụng!");
            }
            if (existingUser.getPhoneNumber() != null && existingUser.getPhoneNumber().equals(registerRequest.getPhoneNumber())) {
                duplicateErrors.put("phoneNumber", "Số điện thoại đã được sử dụng!");
            }
        }

        if (!duplicateErrors.isEmpty()) {
            throw new BadRequestException("Thông tin đăng ký đã tồn tại trên hệ thống!", duplicateErrors);
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .fullName(registerRequest.getFullName())
                .email(registerRequest.getEmail())
                .phoneNumber(registerRequest.getPhoneNumber())
                .role(Role.CUSTOMER)
                .balance(BigDecimal.ZERO)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        return userMapper.toUserDTO(savedUser);
    }

    public void forgotPassword(ForgotPasswordRequest request) throws Exception {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản với email này!"));
        // 1. Sinh mã OTP ngẫu nhiên 6 chữ số
        String otpCode = String.format("%06d", new java.util.Random().nextInt(900000) + 100000);
        // 2. Lưu token vào CSDL (hết hạn trong 10 phút)
        PasswordResetToken token = PasswordResetToken.builder()
                .email(user.getEmail())
                .otpCode(otpCode)
                .expiryDate(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();
        tokenRepository.save(token);
        // 3. Lấy template HTML từ Frontend hoặc dùng template mặc định nếu trống
        String htmlContent;
        if (request.getHtmlTemplate() != null && !request.getHtmlTemplate().isBlank()) {
            htmlContent = request.getHtmlTemplate().replace("{{OTP}}", otpCode);
        } else {
            htmlContent = "<h3>Mã OTP xác thực của bạn là: <b>" + otpCode + "</b> (Hiệu lực trong 10 phút)</h3>";
        }
        // 4. Gửi email
        emailService.sendHtmlEmail(user.getEmail(), "Mã Xác Nhận Đặt Lại Mật Khẩu - Căn Tin Smart", htmlContent);
    }
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = tokenRepository.findByEmailAndOtpCodeAndUsedFalse(request.getEmail(), request.getOtpCode())
                .orElseThrow(() -> new BadRequestException("Mã OTP không chính xác hoặc đã được sử dụng!"));
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Mã OTP đã hết hạn sử dụng! Vui lòng xin mã mới.");
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản!"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        token.setUsed(true);
        tokenRepository.save(token);
    }
}