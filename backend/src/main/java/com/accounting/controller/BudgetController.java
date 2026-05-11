package com.accounting.controller;

import com.accounting.dto.ApiResponse;
import com.accounting.dto.BudgetDTO;
import com.accounting.service.AuthService;
import com.accounting.service.BudgetService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预算控制器
 */
@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private static final Logger logger = LoggerFactory.getLogger(BudgetController.class);

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private AuthService authService;

    /**
     * 获取月度预算列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BudgetDTO>>> getBudgets(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        Long userId = authService.getCurrentUserId();
        List<BudgetDTO> budgets = budgetService.getMonthlyBudgets(userId, year, month);
        return ResponseEntity.ok(ApiResponse.success(budgets));
    }

    /**
     * 获取预算概览
     */
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<List<BudgetDTO>>> getBudgetOverview(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        Long userId = authService.getCurrentUserId();
        List<BudgetDTO> budgets = budgetService.getBudgetOverview(userId, year, month);
        return ResponseEntity.ok(ApiResponse.success(budgets));
    }

    /**
     * 创建或更新预算
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BudgetDTO>> saveBudget(@Valid @RequestBody BudgetDTO dto) {
        Long userId = authService.getCurrentUserId();
        BudgetDTO budget = budgetService.saveBudget(userId, dto, authService.getCurrentUser());
        return ResponseEntity.ok(ApiResponse.success("预算保存成功", budget));
    }

    /**
     * 删除预算
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(@PathVariable Long id) {
        Long userId = authService.getCurrentUserId();
        budgetService.deleteBudget(userId, id);
        return ResponseEntity.ok(ApiResponse.success("预算删除成功", null));
    }
}
