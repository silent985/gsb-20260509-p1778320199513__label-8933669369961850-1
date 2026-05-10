package com.accounting.controller;

import com.accounting.dto.AccountDTO;
import com.accounting.dto.ApiResponse;
import com.accounting.service.AccountService;
import com.accounting.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 账户控制器
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private AccountService accountService;

    @Autowired
    private AuthService authService;

    /**
     * 获取所有账户
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountDTO>>> getAccounts() {
        Long userId = authService.getCurrentUserId();
        List<AccountDTO> accounts = accountService.getUserAccounts(userId);
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    /**
     * 获取单个账户
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountDTO>> getAccount(@PathVariable Long id) {
        Long userId = authService.getCurrentUserId();
        AccountDTO account = accountService.getAccount(userId, id);
        return ResponseEntity.ok(ApiResponse.success(account));
    }

    /**
     * 创建账户
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AccountDTO>> createAccount(@Valid @RequestBody AccountDTO dto) {
        Long userId = authService.getCurrentUserId();
        AccountDTO account = accountService.createAccount(userId, dto, authService.getCurrentUser());
        return ResponseEntity.ok(ApiResponse.success("账户创建成功", account));
    }

    /**
     * 更新账户
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountDTO>> updateAccount(
            @PathVariable Long id, @Valid @RequestBody AccountDTO dto) {
        Long userId = authService.getCurrentUserId();
        AccountDTO account = accountService.updateAccount(userId, id, dto);
        return ResponseEntity.ok(ApiResponse.success("账户更新成功", account));
    }

    /**
     * 删除账户
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@PathVariable Long id) {
        Long userId = authService.getCurrentUserId();
        accountService.deleteAccount(userId, id);
        return ResponseEntity.ok(ApiResponse.success("账户删除成功", null));
    }
}
