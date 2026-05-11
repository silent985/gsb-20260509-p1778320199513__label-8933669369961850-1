package com.accounting.service;

import com.accounting.dto.BudgetDTO;
import com.accounting.entity.Budget;
import com.accounting.entity.Category;
import com.accounting.entity.User;
import com.accounting.exception.BusinessException;
import com.accounting.exception.ResourceNotFoundException;
import com.accounting.repository.BudgetRepository;
import com.accounting.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 预算服务
 */
@Service
public class BudgetService {

    private static final Logger logger = LoggerFactory.getLogger(BudgetService.class);

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * 获取月度预算列表
     */
    @Transactional(readOnly = true)
    public List<BudgetDTO> getMonthlyBudgets(Long userId, Integer year, Integer month) {
        return budgetRepository.findByUserIdAndYearAndMonth(userId, year, month)
                .stream()
                .map(BudgetDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 创建或更新预算
     */
    @Transactional
    public BudgetDTO saveBudget(Long userId, BudgetDTO dto, User user) {
        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryService.getCategoryEntity(userId, dto.getCategoryId());
        }

        // 查找是否已存在相同的预算
        Optional<Budget> existingBudget;
        if (dto.getCategoryId() != null) {
            existingBudget = budgetRepository.findByUserIdAndCategoryIdAndYearAndMonth(
                    userId, dto.getCategoryId(), dto.getYear(), dto.getMonth());
        } else {
            existingBudget = budgetRepository.findByUserIdAndCategoryIsNullAndYearAndMonth(
                    userId, dto.getYear(), dto.getMonth());
        }

        Budget budget;
        if (existingBudget.isPresent()) {
            // 更新已有预算
            budget = existingBudget.get();
            budget.setAmount(dto.getAmount());
            if (dto.getAlertThreshold() != null) {
                budget.setAlertThreshold(dto.getAlertThreshold());
            }
        } else {
            // 创建新预算
            budget = Budget.builder()
                    .user(user)
                    .category(category)
                    .amount(dto.getAmount())
                    .spentAmount(BigDecimal.ZERO)
                    .period(dto.getPeriod() != null ? dto.getPeriod() : "MONTHLY")
                    .year(dto.getYear())
                    .month(dto.getMonth())
                    .alertThreshold(dto.getAlertThreshold() != null ? dto.getAlertThreshold() : 80)
                    .build();

            // 计算已花费金额
            if (category != null) {
                LocalDate startDate = LocalDate.of(dto.getYear(), dto.getMonth(), 1);
                LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
                BigDecimal spent = transactionRepository.getCategoryExpenseByDateRange(
                        userId, category.getId(), startDate, endDate);
                budget.setSpentAmount(spent);
            }
        }

        budget = budgetRepository.save(budget);
        logger.info("Budget saved for category: {} year: {} month: {} user: {}", 
                dto.getCategoryId(), dto.getYear(), dto.getMonth(), userId);

        return BudgetDTO.fromEntity(budget);
    }

    /**
     * 删除预算
     */
    @Transactional
    public void deleteBudget(Long userId, Long budgetId) {
        Budget budget = budgetRepository.findByIdAndUserId(budgetId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("预算", budgetId));

        budgetRepository.delete(budget);
        logger.info("Budget deleted: {} for user: {}", budgetId, userId);
    }

    /**
     * 更新预算使用金额
     */
    @Transactional
    public void updateSpentAmount(Long userId, Long categoryId, LocalDate date, BigDecimal amount, boolean isAdd) {
        Optional<Budget> budgetOpt = budgetRepository.findByUserIdAndCategoryIdAndYearAndMonth(
                userId, categoryId, date.getYear(), date.getMonthValue());

        if (budgetOpt.isPresent()) {
            Budget budget = budgetOpt.get();
            if (isAdd) {
                budget.setSpentAmount(budget.getSpentAmount().add(amount));
            } else {
                budget.setSpentAmount(budget.getSpentAmount().subtract(amount));
                if (budget.getSpentAmount().compareTo(BigDecimal.ZERO) < 0) {
                    budget.setSpentAmount(BigDecimal.ZERO);
                }
            }
            budgetRepository.save(budget);
            logger.debug("Budget spent amount updated for category: {} to: {}", categoryId, budget.getSpentAmount());
        }
    }

    /**
     * 获取预算概览
     */
    @Transactional(readOnly = true)
    public List<BudgetDTO> getBudgetOverview(Long userId, Integer year, Integer month) {
        List<Budget> budgets = budgetRepository.findBudgetsByUserIdAndPeriod(userId, year, month);
        return budgets.stream()
                .map(BudgetDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
