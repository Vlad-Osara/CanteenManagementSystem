package com.canteenbackend.api.auth.repository;

import com.canteenbackend.api.auth.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByEmailAndOtpCodeAndUsedFalse(String email, String otpCode);
}