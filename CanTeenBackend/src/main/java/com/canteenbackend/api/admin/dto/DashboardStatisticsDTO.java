package com.canteenbackend.api.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatisticsDTO implements Serializable {
    private int year;
    private int currentMonth;
    private BigDecimal totalYearDeposit;
    private BigDecimal totalYearRevenue;
    private long totalYearOrders;
    private List<MonthlyStatsDTO> monthlyStats;
}
