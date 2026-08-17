package com.canteenbackend.api.user.mapper;

import com.canteenbackend.api.user.dto.UserDTO;
import com.canteenbackend.api.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .balance(user.getBalance())
                .build();
    }
}
