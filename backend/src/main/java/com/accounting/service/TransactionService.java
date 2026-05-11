package com.accounting.service;

import com.accounting.dto.TransactionDTO;
import com.accounting.entity.Account;
import com.accounting.entity.Category;
import com.accounting.entity.Transaction;
import com.accounting.entity.User;
import com.accounting.exception.ResourceNotFoundException;
import com.accounting.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 交易记录服务
 */
@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BudgetService budgetService;

    /**
     * 获取交易记录分页列表
     */
    @Transactional(readOnly = true)
    public Page<TransactionDTO> getTransactions(Long userId, Pageable pageable) {
        return transactionRepository.findByUserIdOrderByTransactionDateDescCreatedAtDesc(userId, pageable)
                .map(TransactionDTO::fromEntity);
    }

    /**
     * 获取日期范围内的交易记录
     */
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByUserIdAndTransactionDateBetween(userId, startDate, endDate)
                .stream()
                .map(TransactionDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 创建交易记录
     */
    @Transactional
    public TransactionDTO createTransaction(Long userId, TransactionDTO dto, User user) {
        Account account = accountService.getAccountEntity(userId, dto.getAccountId());
        Category category = categoryService.getCategoryEntity(userId, dto.getCategoryId());

        Transaction transaction = Transaction.builder()
                .user(user)
                .account(account)
                .category(category)
                .type(dto.getType())
                .amount(dto.getAmount())
                .transactionDate(dto.getTransactionDate())
                .note(dto.getNote())
                .tags(dto.getTags())
                .build();

        transaction = transactionRepository.save(transaction);

        // 更新账户余额
        boolean isIncome = "INCOME".equals(dto.getType());
        accountService.updateBalance(account.getId(), dto.getAmount(), isIncome);

        // 更新预算使用金额（仅支出）
        if (!isIncome) {
            budgetService.updateSpentAmount(userId, category.getId(), dto.getTransactionDate(), dto.getAmount(), true);
        }

        logger.info("Transaction created: {} {} for user: {}", dto.getType(), dto.getAmount(), userId);

        return TransactionDTO.fromEntity(transaction);
    }

    /**
     * 更新交易记录
     */
    @Transactional
    public TransactionDTO updateTransaction(Long userId, Long transactionId, TransactionDTO dto) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("交易记录", transactionId));

        // 恢复原账户余额
        boolean wasIncome = "INCOME".equals(transaction.getType());
        accountService.updateBalance(transaction.getAccount().getId(), transaction.getAmount(), !wasIncome);

        // 如果是支出，恢复原预算使用金额
        if (!wasIncome) {
            budgetService.updateSpentAmount(userId, transaction.getCategory().getId(), 
                    transaction.getTransactionDate(), transaction.getAmount(), false);
        }

        // 更新记录
        Account account = accountService.getAccountEntity(userId, dto.getAccountId());
        Category category = categoryService.getCategoryEntity(userId, dto.getCategoryId());

        transaction.setAccount(account);
        transaction.setCategory(category);
        transaction.setType(dto.getType());
        transaction.setAmount(dto.getAmount());
        transaction.setTransactionDate(dto.getTransactionDate());
        transaction.setNote(dto.getNote());
        transaction.setTags(dto.getTags());

        transaction = transactionRepository.save(transaction);

        // 更新新账户余额
        boolean isIncome = "INCOME".equals(dto.getType());
        accountService.updateBalance(account.getId(), dto.getAmount(), isIncome);

        // 更新新预算使用金额（仅支出）
        if (!isIncome) {
            budgetService.updateSpentAmount(userId, category.getId(), dto.getTransactionDate(), dto.getAmount(), true);
        }

        logger.info("Transaction updated: {} for user: {}", transactionId, userId);

        return TransactionDTO.fromEntity(transaction);
    }

    /**
     * 删除交易记录
     */
    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("交易记录", transactionId));

        // 恢复账户余额
        boolean isIncome = "INCOME".equals(transaction.getType());
        accountService.updateBalance(transaction.getAccount().getId(), transaction.getAmount(), !isIncome);

        // 恢复预算使用金额（仅支出）
        if (!isIncome) {
            budgetService.updateSpentAmount(userId, transaction.getCategory().getId(), 
                    transaction.getTransactionDate(), transaction.getAmount(), false);
        }

        transactionRepository.delete(transaction);
        logger.info("Transaction deleted: {} for user: {}", transactionId, userId);
    }

    /**
     * 获取单个交易记录
     */
    @Transactional(readOnly = true)
    public TransactionDTO getTransaction(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("交易记录", transactionId));
        return TransactionDTO.fromEntity(transaction);
    }
}
