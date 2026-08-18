package com.canteenbackend.api.transaction.repository;

import com.canteenbackend.api.transaction.model.Transaction;
import com.canteenbackend.api.transaction.model.TransactionType;
import com.canteenbackend.helper.base.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface TransactionRepository extends BaseRepository<Transaction, UUID> {

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.type = :type AND t.description LIKE '%thành công%' " +
            "AND YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month")
    BigDecimal sumDepositByYearAndMonth(@Param("type") TransactionType type, @Param("year") int year, @Param("month") int month);
}