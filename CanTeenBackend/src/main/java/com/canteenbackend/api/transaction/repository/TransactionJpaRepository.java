package com.canteenbackend.api.transaction.repository;

import com.canteenbackend.api.transaction.model.Transaction;
import com.canteenbackend.api.transaction.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionJpaRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findById(UUID id);
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.type = :type AND t.description LIKE '%thành công%' " +
            "AND YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month")
    BigDecimal sumDepositByYearAndMonth(@Param("type") TransactionType type,
                                        @Param("year") int year,
                                        @Param("month") int month);
}
