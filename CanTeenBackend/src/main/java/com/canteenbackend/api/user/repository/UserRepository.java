package com.canteenbackend.api.user.repository;

import com.canteenbackend.api.user.model.User;
import com.canteenbackend.helper.base.model.Role;
import com.canteenbackend.helper.base.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepository extends BaseRepository<User, UUID, UserJpaRepository> {

    public UserRepository(UserJpaRepository userJpaRepository) {
        super(userJpaRepository, User.class);
    }

    public Optional<User> findByUsername(String username) {

        return repository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public Boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    public List<User> findExistingUsers(String username, String email, String phoneNumber) {
        return repository.findExistingUsers(username, email, phoneNumber);
    }

    public Boolean existsByUsername(String username) {
        return repository.existsByUsername(username);
    }

    public Page<User> findByRole(Role role, Pageable pageable) {
        return repository.findByRole(role, pageable);
    }

    public int deductBalance(UUID userId, BigDecimal amount) {
        return repository.deductBalance(userId, amount);
    }

    public int addBalance(UUID userId, BigDecimal amount) {
        return repository.addBalance(userId, amount);
    }
}
