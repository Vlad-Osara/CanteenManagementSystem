package com.canteenbackend.utils.security;

import com.canteenbackend.api.user.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Getter // Dùng để lấy getId() sau này
public class CustomUserDetails implements UserDetails {

    private final UUID id; // <--- Trường quan trọng nhất để chống IDOR không tốn Query
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    // Constructor map từ Entity User của bạn sang CustomUserDetails
    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.username = user.getUsername(); // hoặc user.getEmail() tùy bạn cấu hình đăng nhập bằng gì
        this.password = user.getPassword();
        // Giả sử bạn lưu Role dạng String trong Entity (Ví dụ: "ADMIN", "USER")
        this.authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
