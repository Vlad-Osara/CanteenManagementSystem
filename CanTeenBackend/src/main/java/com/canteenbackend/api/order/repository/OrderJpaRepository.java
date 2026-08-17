package com.canteenbackend.api.order.repository;

import com.canteenbackend.api.order.model.Order;
import com.canteenbackend.api.order.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderJpaRepository extends JpaRepository <Order, UUID>{
    Page<Order> findByCustomerId(UUID userId, Pageable pageable);

    Page<Order> findByStatusIn(List<OrderStatus> status, Pageable pageable);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o " +
            "WHERE o.status = :status AND YEAR(o.createdAt) = :year AND MONTH(o.createdAt) = :month")
    BigDecimal sumRevenueByYearAndMonth(@Param("status") OrderStatus status,
                                        @Param("year") int year,
                                        @Param("month") int month);

    @Query("SELECT COUNT(o) FROM Order o " +
            "WHERE o.status = :status AND YEAR(o.createdAt) = :year AND MONTH(o.createdAt) = :month")
    long countOrdersByYearAndMonth(@Param("status") OrderStatus status,
                                   @Param("year") int year,
                                   @Param("month") int month);

    @Query("SELECT c.name, COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi " +
            "JOIN oi.order o " +
            "JOIN oi.dish d " +
            "JOIN d.category c " +
            "WHERE o.status = :status AND YEAR(o.createdAt) = :year AND MONTH(o.createdAt) = :month " +
            "GROUP BY c.name")
    List<Object[]> countCategorySalesByYearAndMonth(@Param("status") OrderStatus status,
                                                    @Param("year") int year,
                                                    @Param("month") int month);
}
