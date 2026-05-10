package com.accounting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 统计数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDTO {

    // 总览数据
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netAmount;
    private BigDecimal totalBalance;

    // 本月数据
    private BigDecimal monthIncome;
    private BigDecimal monthExpense;
    private BigDecimal monthNet;

    // 今日数据
    private BigDecimal todayIncome;
    private BigDecimal todayExpense;

    // 本年数据
    private BigDecimal yearIncome;
    private BigDecimal yearExpense;

    // 分类统计
    private List<CategoryStatDTO> incomeByCategory;
    private List<CategoryStatDTO> expenseByCategory;

    // 趋势数据
    private List<TrendDataDTO> monthlyTrend;
    private List<TrendDataDTO> dailyTrend;

    // 账户余额
    private List<AccountBalanceDTO> accountBalances;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryStatDTO {
        private Long categoryId;
        private String categoryName;
        private String categoryIcon;
        private String categoryColor;
        private BigDecimal amount;
        private BigDecimal percentage;
        private Integer count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendDataDTO {
        private String label; // 日期标签
        private BigDecimal income;
        private BigDecimal expense;
        private BigDecimal net;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountBalanceDTO {
        private Long accountId;
        private String accountName;
        private String accountType;
        private String accountIcon;
        private String accountColor;
        private BigDecimal balance;
    }
}
