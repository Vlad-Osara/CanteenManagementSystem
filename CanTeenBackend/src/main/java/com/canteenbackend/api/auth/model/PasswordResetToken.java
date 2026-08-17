package com.canteenbackend.api.auth.model;

import com.canteenbackend.helper.base.model.BaseModel;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken extends BaseModel {

    private String email;
    private String otpCode;
    private LocalDateTime expiryDate;
    private boolean used;
}