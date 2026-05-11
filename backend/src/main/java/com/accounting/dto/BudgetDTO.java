package com.accounting.dto;

import com.accounting.entity.Budget;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预算DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetDTO {

    private Long id;

    private Long categoryId;
    private String categoryName;
    private String categoryIcon;

    @NotNull(message = "预算金额不能为空")
    @Positive(message = "预算金额必须大于0")
    private BigDecimal amount;

    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private BigDecimal usagePercentage;

    private String period; // MONTHLY, YEARLY

    @NotNull(message = "年份不能为空")
    private Integer year;

    private Integer month;
    private Integer alertThreshold;
    private Boolean isOverBudget;
    private Boolean isAlertTriggered;
    private LocalDateTime createdAt;

    public static BudgetDTO fromEntity(Budget budget) {
        BigDecimal remaining = budget.getAmount().subtract(budget.getSpentAmount());
        return BudgetDTO.builder()
                .id(budget.getId())
                .categoryId(budget.getCategory() != null ? budget.getCategory().getId() : null)
                .categoryName(budget.getCategory() != null ? budget.getCategory().getName() : "总预算")
                .categoryIcon(budget.getCategory() != null ? budget.getCategory().getIcon() : null)
                .amount(budget.getAmount())
                .spentAmount(budget.getSpentAmount())
                .remainingAmount(remaining.abs())
                .usagePercentage(budget.getUsagePercentage())
                .period(budget.getPeriod())
                .year(budget.getYear())
                .month(budget.getMonth())
                .alertThreshold(budget.getAlertThreshold())
                .isOverBudget(budget.isOverBudget())
                .isAlertTriggered(budget.isAlertTriggered())
                .createdAt(budget.getCreatedAt())
                .build();
    }
}
