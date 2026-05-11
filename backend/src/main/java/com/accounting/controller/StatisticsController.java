package com.accounting.controller;

import com.accounting.dto.ApiResponse;
import com.accounting.dto.StatisticsDTO;
import com.accounting.service.AuthService;
import com.accounting.service.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 统计控制器
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private AuthService authService;

    /**
     * 获取统计概览
     */
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<StatisticsDTO>> getOverview() {
        Long userId = authService.getCurrentUserId();
        StatisticsDTO statistics = statisticsService.getOverview(userId);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    /**
     * 获取分类统计
     */
    @GetMapping("/category")
    public ResponseEntity<ApiResponse<StatisticsDTO>> getCategoryStats(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        Long userId = authService.getCurrentUserId();
        StatisticsDTO statistics = statisticsService.getCategoryStats(userId, year, month);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    /**
     * 获取月度趋势
     */
    @GetMapping("/trend/monthly")
    public ResponseEntity<ApiResponse<StatisticsDTO>> getMonthlyTrend(
            @RequestParam(defaultValue = "12") Integer months) {
        Long userId = authService.getCurrentUserId();
        StatisticsDTO statistics = statisticsService.getMonthlyTrend(userId, months);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    /**
     * 获取日趋势
     */
    @GetMapping("/trend/daily")
    public ResponseEntity<ApiResponse<StatisticsDTO>> getDailyTrend(
            @RequestParam(defaultValue = "30") Integer days) {
        Long userId = authService.getCurrentUserId();
        StatisticsDTO statistics = statisticsService.getDailyTrend(userId, days);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }
}
