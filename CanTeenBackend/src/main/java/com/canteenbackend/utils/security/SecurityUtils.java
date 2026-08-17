package com.canteenbackend.utils.security;

import com.canteenbackend.api.user.model.User;
import com.canteenbackend.api.user.repository.UserRepository;
import com.canteenbackend.exceptions.custom.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.security.autoconfigure.SecurityProperties;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
public class SecurityUtils {
    private final UserRepository userRepository;

    /**
     * Lấy nhanh thông tin UserDetails từ SecurityContext (Dữ liệu nằm trên RAM, không gọi DB)
     */
    public CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new AccessDeniedException("Người dùng chưa xác thực");
        }

        return (CustomUserDetails) authentication.getPrincipal();
    }

    /**
     * Chỉ dùng hàm này khi THỰC SỰ cần Entity User (ví dụ: gán vào order.setCustomer(user))
     */
    public User getCurrentUserEntity() {
        CustomUserDetails userDetails = getCurrentUserDetails();
        // Tìm kiếm bằng ID sẽ nhanh hơn tìm kiếm bằng String Username/Email
        return userRepository.get(userDetails.getId());
    }
}
