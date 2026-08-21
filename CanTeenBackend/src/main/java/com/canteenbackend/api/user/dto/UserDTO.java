package com.canteenbackend.api.user.dto;

import com.canteenbackend.helper.base.dto.BaseDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder
public class UserDTO extends BaseDTO {
    private String fullName;
    private String email;
    private String phoneNumber;
    private BigDecimal balance;
    private String role;
    private Boolean isActive;
}
