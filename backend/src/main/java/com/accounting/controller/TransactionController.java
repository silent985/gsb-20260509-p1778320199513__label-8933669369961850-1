package com.accounting.controller;

import com.accounting.dto.ApiResponse;
import com.accounting.dto.TransactionDTO;
import com.accounting.service.AuthService;
import com.accounting.service.TransactionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 交易记录控制器
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AuthService authService;

    /**
     * 获取交易记录列表（分页）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransactionDTO>>> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = authService.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionDTO> transactions = transactionService.getTransactions(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    /**
     * 获取日期范围内的交易记录
     */
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<TransactionDTO>>> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = authService.getCurrentUserId();
        List<TransactionDTO> transactions = transactionService.getTransactionsByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    /**
     * 获取单个交易记录
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionDTO>> getTransaction(@PathVariable Long id) {
        Long userId = authService.getCurrentUserId();
        TransactionDTO transaction = transactionService.getTransaction(userId, id);
        return ResponseEntity.ok(ApiResponse.success(transaction));
    }

    /**
     * 创建交易记录
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TransactionDTO>> createTransaction(
            @Valid @RequestBody TransactionDTO dto) {
        Long userId = authService.getCurrentUserId();
        TransactionDTO transaction = transactionService.createTransaction(userId, dto, authService.getCurrentUser());
        return ResponseEntity.ok(ApiResponse.success("记录创建成功", transaction));
    }

    /**
     * 更新交易记录
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionDTO>> updateTransaction(
            @PathVariable Long id, @Valid @RequestBody TransactionDTO dto) {
        Long userId = authService.getCurrentUserId();
        TransactionDTO transaction = transactionService.updateTransaction(userId, id, dto);
        return ResponseEntity.ok(ApiResponse.success("记录更新成功", transaction));
    }

    /**
     * 删除交易记录
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(@PathVariable Long id) {
        Long userId = authService.getCurrentUserId();
        transactionService.deleteTransaction(userId, id);
        return ResponseEntity.ok(ApiResponse.success("记录删除成功", null));
    }
}
