package com.canteenbackend.api.admin;

import com.canteenbackend.api.admin.service.StatisticsService;
import com.canteenbackend.helper.base.response.ResponseObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StatisticsController {
    private final StatisticsService statisticsService;
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStatistics(
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(ResponseObject.success(
                "Lấy dữ liệu thống kê biểu đồ thành công",
                statisticsService.getYearlyDashboardStats(year)
        ));
    }
}
