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
public interface UserRepository extends BaseRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Page<User> findByRole(Role role, Pageable pageable);
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.username = :username OR u.email = :email OR u.phoneNumber = :phoneNumber")
    List<User> findExistingUsers(@Param("username") String username,
                                 @Param("email") String email,
                                 @Param("phoneNumber") String phoneNumber);

    @Modifying
    @Query("UPDATE User u SET u.balance = u.balance - :amount WHERE u.id = :userId AND u.balance >= :amount")
    int deductBalance(@Param("userId") UUID userId, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE User u SET u.balance = u.balance + :amount WHERE u.id = :userId")
    int addBalance(@Param("userId") UUID userId, @Param("amount") BigDecimal amount);
}