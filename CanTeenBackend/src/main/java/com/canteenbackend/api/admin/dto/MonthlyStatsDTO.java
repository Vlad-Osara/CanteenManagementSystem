package com.canteenbackend.api.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyStatsDTO implements Serializable {
    private int month;
    private int year;
    private BigDecimal totalDeposit; // Total deposit for the month
    private BigDecimal totalOrderRevenue; // Total revenue from orders for the month
    private long totalOrders; // Total number of orders for the month
    private Map<String, Long> categorySales; // Sales by category for the month
}
