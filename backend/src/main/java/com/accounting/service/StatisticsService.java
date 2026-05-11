package com.accounting.service;

import com.accounting.dto.AccountDTO;
import com.accounting.dto.StatisticsDTO;
import com.accounting.repository.AccountRepository;
import com.accounting.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计服务
 */
@Service
public class StatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

    /**
     * 获取统计概览
     */
    public StatisticsDTO getOverview(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
        LocalDate yearStart = today.withDayOfYear(1);
        LocalDate yearEnd = today.withDayOfYear(today.lengthOfYear());

        // 总收入和支出
        BigDecimal totalIncome = transactionRepository.getTotalIncomeByUserId(userId);
        BigDecimal totalExpense = transactionRepository.getTotalExpenseByUserId(userId);

        // 本月收支
        BigDecimal monthIncome = transactionRepository.getIncomeByUserIdAndDateRange(userId, monthStart, monthEnd);
        BigDecimal monthExpense = transactionRepository.getExpenseByUserIdAndDateRange(userId, monthStart, monthEnd);

        // 今日收支
        BigDecimal todayIncome = transactionRepository.getIncomeByUserIdAndDateRange(userId, today, today);
        BigDecimal todayExpense = transactionRepository.getExpenseByUserIdAndDateRange(userId, today, today);

        // 本年收支
        BigDecimal yearIncome = transactionRepository.getIncomeByUserIdAndDateRange(userId, yearStart, yearEnd);
        BigDecimal yearExpense = transactionRepository.getExpenseByUserIdAndDateRange(userId, yearStart, yearEnd);

        // 总余额
        BigDecimal totalBalance = accountService.getTotalBalance(userId);

        // 账户余额列表
        List<StatisticsDTO.AccountBalanceDTO> accountBalances = accountService.getUserAccounts(userId)
                .stream()
                .map(a -> StatisticsDTO.AccountBalanceDTO.builder()
                        .accountId(a.getId())
                        .accountName(a.getName())
                        .accountType(a.getType())
                        .accountIcon(a.getIcon())
                        .accountColor(a.getColor())
                        .balance(a.getBalance())
                        .build())
                .collect(Collectors.toList());

        return StatisticsDTO.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netAmount(totalIncome.subtract(totalExpense))
                .totalBalance(totalBalance)
                .monthIncome(monthIncome)
                .monthExpense(monthExpense)
                .monthNet(monthIncome.subtract(monthExpense))
                .todayIncome(todayIncome)
                .todayExpense(todayExpense)
                .yearIncome(yearIncome)
                .yearExpense(yearExpense)
                .accountBalances(accountBalances)
                .build();
    }

    /**
     * 获取分类统计
     */
    public StatisticsDTO getCategoryStats(Long userId, Integer year, Integer month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 支出分类统计
        List<Object[]> expenseStats = transactionRepository.getCategoryStatsByUserIdAndTypeAndDateRange(
                userId, "EXPENSE", startDate, endDate);
        List<StatisticsDTO.CategoryStatDTO> expenseByCategory = buildCategoryStats(expenseStats);

        // 收入分类统计
        List<Object[]> incomeStats = transactionRepository.getCategoryStatsByUserIdAndTypeAndDateRange(
                userId, "INCOME", startDate, endDate);
        List<StatisticsDTO.CategoryStatDTO> incomeByCategory = buildCategoryStats(incomeStats);

        return StatisticsDTO.builder()
                .expenseByCategory(expenseByCategory)
                .incomeByCategory(incomeByCategory)
                .build();
    }

    private List<StatisticsDTO.CategoryStatDTO> buildCategoryStats(List<Object[]> stats) {
        if (stats == null || stats.isEmpty()) {
            return new ArrayList<>();
        }

        // 计算总金额
        BigDecimal total = stats.stream()
                .map(row -> (BigDecimal) row[4])
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return stats.stream()
                .map(row -> StatisticsDTO.CategoryStatDTO.builder()
                        .categoryId((Long) row[0])
                        .categoryName((String) row[1])
                        .categoryIcon((String) row[2])
                        .categoryColor((String) row[3])
                        .amount((BigDecimal) row[4])
                        .percentage(total.compareTo(BigDecimal.ZERO) > 0 ?
                                ((BigDecimal) row[4]).multiply(BigDecimal.valueOf(100))
                                        .divide(total, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                        .count(((Long) row[5]).intValue())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 获取月度趋势
     */
    public StatisticsDTO getMonthlyTrend(Long userId, Integer months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months - 1).withDayOfMonth(1);

        List<Object[]> trendData = transactionRepository.getMonthlyTrendByUserId(userId, startDate, endDate);

        // 按月份分组
        Map<String, StatisticsDTO.TrendDataDTO> trendMap = new HashMap<>();
        
        // 初始化所有月份
        for (int i = 0; i < months; i++) {
            YearMonth ym = YearMonth.from(startDate).plusMonths(i);
            String label = ym.toString();
            trendMap.put(label, StatisticsDTO.TrendDataDTO.builder()
                    .label(label)
                    .income(BigDecimal.ZERO)
                    .expense(BigDecimal.ZERO)
                    .net(BigDecimal.ZERO)
                    .build());
        }

        // 填充数据
        for (Object[] row : trendData) {
            String label = (String) row[0];
            String type = (String) row[1];
            BigDecimal amount = (BigDecimal) row[2];

            StatisticsDTO.TrendDataDTO dto = trendMap.get(label);
            if (dto != null) {
                if ("INCOME".equals(type)) {
                    dto.setIncome(amount);
                } else {
                    dto.setExpense(amount);
                }
                dto.setNet(dto.getIncome().subtract(dto.getExpense()));
            }
        }

        List<StatisticsDTO.TrendDataDTO> monthlyTrend = trendMap.values().stream()
                .sorted((a, b) -> a.getLabel().compareTo(b.getLabel()))
                .collect(Collectors.toList());

        return StatisticsDTO.builder()
                .monthlyTrend(monthlyTrend)
                .build();
    }

    /**
     * 获取日趋势
     */
    public StatisticsDTO getDailyTrend(Long userId, Integer days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<Object[]> trendData = transactionRepository.getDailyTrendByUserId(userId, startDate, endDate);

        // 按日期分组
        Map<LocalDate, StatisticsDTO.TrendDataDTO> trendMap = new HashMap<>();
        
        // 初始化所有日期
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            trendMap.put(date, StatisticsDTO.TrendDataDTO.builder()
                    .label(date.toString())
                    .income(BigDecimal.ZERO)
                    .expense(BigDecimal.ZERO)
                    .net(BigDecimal.ZERO)
                    .build());
        }

        // 填充数据
        for (Object[] row : trendData) {
            LocalDate date = (LocalDate) row[0];
            String type = (String) row[1];
            BigDecimal amount = (BigDecimal) row[2];

            StatisticsDTO.TrendDataDTO dto = trendMap.get(date);
            if (dto != null) {
                if ("INCOME".equals(type)) {
                    dto.setIncome(amount);
                } else {
                    dto.setExpense(amount);
                }
                dto.setNet(dto.getIncome().subtract(dto.getExpense()));
            }
        }

        List<StatisticsDTO.TrendDataDTO> dailyTrend = trendMap.values().stream()
                .sorted((a, b) -> a.getLabel().compareTo(b.getLabel()))
                .collect(Collectors.toList());

        return StatisticsDTO.builder()
                .dailyTrend(dailyTrend)
                .build();
    }
}
