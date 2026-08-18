package com.canteenbackend.api.admin.service;

import com.canteenbackend.api.admin.dto.DashboardStatisticsDTO;
import com.canteenbackend.api.admin.dto.MonthlyStatsDTO;
import com.canteenbackend.api.order.model.OrderStatus;
import com.canteenbackend.api.order.repository.OrderRepository;
import com.canteenbackend.api.transaction.model.TransactionType;
import com.canteenbackend.api.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final TransactionRepository transactionRepository;
    private final OrderRepository orderRepository;

    @Cacheable(
            value = "pastMonthlyStats",
            key = "#year + '_' + #month",
            condition = "(#year < T(java.time.LocalDate).now().getYear()) || " +
                    "(#year == T(java.time.LocalDate).now().getYear() && #month < T(java.time.LocalDate).now().getMonthValue())"
    )
    public MonthlyStatsDTO getStatsForMonth(int year, int month) {
        BigDecimal deposit = transactionRepository.sumDepositByYearAndMonth(TransactionType.DEPOSIT, year, month);
        BigDecimal revenue = orderRepository.sumRevenueByYearAndMonth(OrderStatus.COMPLETED, year, month);
        long orderCount = orderRepository.countOrdersByYearAndMonth(OrderStatus.COMPLETED, year, month);
        List<Object[]> catResults = orderRepository.countCategorySalesByYearAndMonth(OrderStatus.COMPLETED, year, month);
        Map<String, Long> categorySales = new LinkedHashMap<>();
        for (Object[] row : catResults) {
            String categoryName = (String) row[0];
            Number quantity = (Number) row[1];
            categorySales.put(categoryName, quantity != null ? quantity.longValue() : 0L);
        }
        return MonthlyStatsDTO.builder()
                .month(month)
                .year(year)
                .totalDeposit(deposit != null ? deposit : BigDecimal.ZERO)
                .totalOrderRevenue(revenue != null ? revenue : BigDecimal.ZERO)
                .totalOrders(orderCount)
                .categorySales(categorySales)
                .build();
    }

    public DashboardStatisticsDTO getYearlyDashboardStats(Integer requestedYear) {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();

        int year = (requestedYear != null) ? requestedYear : currentYear;

        if (year < 2026) {
            year = 2026;
        }

        int targetMonth = (year == currentYear) ? now.getMonthValue() : (year < currentYear ? 12 : 0);
        List<MonthlyStatsDTO> monthlyStatsList = new ArrayList<>();
        BigDecimal totalYearDeposit = BigDecimal.ZERO;
        BigDecimal totalYearRevenue = BigDecimal.ZERO;
        long totalYearOrders = 0;
        for (int m = 1; m <= targetMonth; m++) {
            MonthlyStatsDTO mStats = getStatsForMonth(year, m);
            monthlyStatsList.add(mStats);
            totalYearDeposit = totalYearDeposit.add(mStats.getTotalDeposit());
            totalYearRevenue = totalYearRevenue.add(mStats.getTotalOrderRevenue());
            totalYearOrders += mStats.getTotalOrders();
        }
        return DashboardStatisticsDTO.builder()
                .year(year)
                .currentMonth(targetMonth)
                .totalYearDeposit(totalYearDeposit)
                .totalYearRevenue(totalYearRevenue)
                .totalYearOrders(totalYearOrders)
                .monthlyStats(monthlyStatsList)
                .build();
    }
}
