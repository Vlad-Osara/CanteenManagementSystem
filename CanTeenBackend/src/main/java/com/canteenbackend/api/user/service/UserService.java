package com.canteenbackend.api.user.service;

import com.canteenbackend.api.user.dto.UserDTO;
import com.canteenbackend.api.user.mapper.UserMapper;
import com.canteenbackend.api.user.model.User;
import com.canteenbackend.api.user.repository.UserRepository;
import com.canteenbackend.api.user.request.UserGetRequest;
import com.canteenbackend.api.user.request.UserStoreRequest;
import com.canteenbackend.api.user.request.UserUpdateRequest;
import com.canteenbackend.exceptions.custom.BadRequestException;
import com.canteenbackend.helper.base.construct.RestfullService;
import com.canteenbackend.helper.base.model.Role;
import com.canteenbackend.utils.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService extends RestfullService<UserDTO, UserGetRequest, UserStoreRequest, UserUpdateRequest> {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final SecurityUtils securityUtils;
    private final HttpServletRequest request;
    @Override
    public Page<UserDTO> getAll(UserGetRequest userGetRequest) {
        Pageable pageable = userGetRequest.toPageable();
        Page<User> users;
        if (userGetRequest.getRole() != null) {
            users = userRepository.findByRole(userGetRequest.getRole(), pageable);
        } else {
            users = userRepository.getAll(pageable);
        }
        return users.map(userMapper::toUserDTO);
    }

    @Override
    public UserDTO get(UUID id) {
        return userMapper.toUserDTO(userRepository.get(id));
    }

    @Override
    public UserDTO store(UserStoreRequest userStoreRequest) {
        List<User> existingUsers = userRepository.findExistingUsers(
                userStoreRequest.getUsername(),
                userStoreRequest.getEmail(),
                userStoreRequest.getPhoneNumber()
        );

        Map<String, String> duplicateErrors = new HashMap<>();

        for (User existingUser : existingUsers) {
            if (existingUser.getUsername().equalsIgnoreCase(userStoreRequest.getUsername())) {
                duplicateErrors.put("username", "Tên tài khoản đã được sử dụng!");
            }
            if (existingUser.getEmail().equalsIgnoreCase(userStoreRequest.getEmail())) {
                duplicateErrors.put("email", "Email đã được sử dụng!");
            }
            if (existingUser.getPhoneNumber() != null && existingUser.getPhoneNumber().equals(userStoreRequest.getPhoneNumber())) {
                duplicateErrors.put("phoneNumber", "Số điện thoại đã được sử dụng!");
            }
        }

        if (!duplicateErrors.isEmpty()) {
            throw new BadRequestException("Thông tin đăng ký đã tồn tại trên hệ thống!", duplicateErrors);
        }

        User user = User.builder()
                .username(userStoreRequest.getUsername())
                .password(passwordEncoder.encode(userStoreRequest.getPassword()))
                .fullName(userStoreRequest.getFullName())
                .email(userStoreRequest.getEmail())
                .phoneNumber(userStoreRequest.getPhoneNumber())
                .role(userStoreRequest.getRole())
                .balance(BigDecimal.ZERO)
                .build();
        return userMapper.toUserDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDTO update(UUID id, UserUpdateRequest userUpdateRequest) {
        // 1. Lấy thông tin kẻ đang thực hiện hành động (Admin hoặc chính User đó) và mục tiêu
        User actor = securityUtils.getCurrentUserEntity();
        User targetUser = userRepository.get(id);

        // Kiểm tra xem kẻ thực hiện có phải là ADMIN không
        boolean isAdmin = actor.getRole() == Role.ADMIN;
        boolean isTargetAdmin = targetUser.getRole() == Role.ADMIN;

        // 2. Chống sửa chéo tài khoản (Chỉ chặn nếu KHÔNG PHẢI là Admin VÀ sửa của người khác)
        if (!isAdmin && !actor.getId().equals(id)) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh sửa tài khoản của người khác!");
        }

        // Chống admin tự hạ quyền
        if (actor.getId().equals(targetUser.getId()) && isAdmin) {
            if (userUpdateRequest.getRole() != null && userUpdateRequest.getRole() != Role.ADMIN) {
                throw new BadRequestException("Bạn không thể tự hạ quyền Quản trị viên của chính mình!");
            }
        }

        String confirmPassword = userUpdateRequest.getConfirmPassword();
        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            throw new BadRequestException("Vui lòng nhập mật khẩu hiện tại để xác thực thay đổi!");
        }
        if (!passwordEncoder.matches(confirmPassword, actor.getPassword())) {
            throw new BadRequestException("Mật khẩu xác thực không chính xác!");
        }

        // Kiểm tra trùng lặp email và sđt với tk khác
        List<User> existingUsers = userRepository.findExistingUsers(
                null,
                userUpdateRequest.getEmail(),
                userUpdateRequest.getPhoneNumber()
        );

        Map<String, String> duplicateErrors = new HashMap<>();

        for (User existingUser : existingUsers) {
            if (!existingUser.getId().equals(id)) {
                if (userUpdateRequest.getEmail() != null &&
                        existingUser.getEmail().equalsIgnoreCase(userUpdateRequest.getEmail())) {
                    duplicateErrors.put("email", "Email đã được sử dụng!");
                }
                if (userUpdateRequest.getPhoneNumber() != null &&
                        existingUser.getPhoneNumber() != null &&
                        existingUser.getPhoneNumber().equals(userUpdateRequest.getPhoneNumber())) {
                    duplicateErrors.put("phoneNumber", "Số điện thoại đã được sử dụng!");
                }
            }
        }

        if (!duplicateErrors.isEmpty()) {
            throw new BadRequestException("Thông tin đăng ký đã tồn tại trên hệ thống!", duplicateErrors);
        }

        // 3. Mặc định chỉ ADMIN mới được phép thay đổi Role và Balance.
        // Nếu là User thường thì ép buộc giữ nguyên giá trị cũ (không cho phép tự thăng chức hay tự cộng tiền)
        Role finalRole = targetUser.getRole(); // Mặc định giữ nguyên Role cũ
        if (isAdmin && !isTargetAdmin) {
            // Chỉ cho phép đổi Role nếu tài khoản mục tiêu KHÔNG PHẢI là Admin
            finalRole = userUpdateRequest.getRole() != null ? userUpdateRequest.getRole() : targetUser.getRole();
        }
        BigDecimal finalBalance = isAdmin ? userUpdateRequest.getBalance() : targetUser.getBalance();
        // 4. Cập nhật dữ liệu
        targetUser.setFullName(userUpdateRequest.getFullName());
        targetUser.setEmail(userUpdateRequest.getEmail());
        targetUser.setPhoneNumber(userUpdateRequest.getPhoneNumber());
        targetUser.setRole(finalRole);
        targetUser.setBalance(finalBalance);

        // 4. Xử lý logic mật khẩu
        String newPassword = userUpdateRequest.getNewPassword();
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            if (passwordEncoder.matches(newPassword, targetUser.getPassword())) {
                throw new BadRequestException("Mật khẩu mới không được trùng với mật khẩu hiện tại!");
            }
            targetUser.setPassword(passwordEncoder.encode(newPassword));
        }

        // 5. Cập nhật bản ghi thông qua ID mục tiêu truyền vào từ URL
        User updatedUser = userRepository.update(id, targetUser);
        return userMapper.toUserDTO(updatedUser);
    }

    @Override
    public UserDTO destroy(UUID id) {
        User actor = securityUtils.getCurrentUserEntity();
        boolean isAdmin = actor.getRole() == Role.ADMIN;
        if (!isAdmin && !actor.getId().equals(id)) {
            throw new AccessDeniedException("Bạn không có quyền chỉnh xóa tài khoản của người khác!");
        }

        String confirmPassword = request.getHeader("X-Confirm-Password");

        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            throw new BadRequestException("Vui lòng nhập mật khẩu admin!");
        }
        if (!passwordEncoder.matches(confirmPassword, actor.getPassword())) {
            throw new BadRequestException("Mật khẩu xác thực của admin không chính xác!");
        }

        User targetUser = userRepository.get(id);
        if (targetUser.getRole() == Role.ADMIN) {
            throw new BadRequestException("Không thể xóa tài khoản Quản trị viên (Admin) để đảm bảo an toàn hệ thống!");
        }
        return userMapper.toUserDTO(userRepository.delete(id));
    }
}
