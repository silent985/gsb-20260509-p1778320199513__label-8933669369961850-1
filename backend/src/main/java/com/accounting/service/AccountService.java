package com.accounting.service;

import com.accounting.dto.AccountDTO;
import com.accounting.entity.Account;
import com.accounting.entity.User;
import com.accounting.exception.BusinessException;
import com.accounting.exception.ResourceNotFoundException;
import com.accounting.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 账户服务
 */
@Service
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    @Autowired
    private AccountRepository accountRepository;

    /**
     * 初始化默认账户
     */
    @Transactional
    public void initDefaultAccount(User user) {
        Account account = Account.builder()
                .user(user)
                .name("现金")
                .type("CASH")
                .balance(BigDecimal.ZERO)
                .icon("💵")
                .color("#52c41a")
                .isDefault(true)
                .sortOrder(0)
                .build();
        accountRepository.save(account);
        logger.info("Default account created for user: {}", user.getUsername());
    }

    /**
     * 获取用户所有账户
     */
    public List<AccountDTO> getUserAccounts(Long userId) {
        return accountRepository.findByUserIdOrderBySortOrderAsc(userId)
                .stream()
                .map(AccountDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 创建账户
     */
    @Transactional
    public AccountDTO createAccount(Long userId, AccountDTO dto, User user) {
        // 检查账户名是否重复
        if (accountRepository.existsByNameAndUserId(dto.getName(), userId)) {
            throw new BusinessException("账户名称已存在");
        }

        Account account = Account.builder()
                .user(user)
                .name(dto.getName())
                .type(dto.getType())
                .balance(dto.getBalance() != null ? dto.getBalance() : BigDecimal.ZERO)
                .icon(dto.getIcon())
                .color(dto.getColor())
                .isDefault(false)
                .note(dto.getNote())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .build();

        account = accountRepository.save(account);
        logger.info("Account created: {} for user: {}", account.getName(), userId);

        return AccountDTO.fromEntity(account);
    }

    /**
     * 更新账户
     */
    @Transactional
    public AccountDTO updateAccount(Long userId, Long accountId, AccountDTO dto) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("账户", accountId));

        // 检查账户名是否与其他账户重复
        if (!account.getName().equals(dto.getName()) && 
            accountRepository.existsByNameAndUserId(dto.getName(), userId)) {
            throw new BusinessException("账户名称已存在");
        }

        account.setName(dto.getName());
        account.setType(dto.getType());
        account.setIcon(dto.getIcon());
        account.setColor(dto.getColor());
        account.setNote(dto.getNote());
        if (dto.getSortOrder() != null) {
            account.setSortOrder(dto.getSortOrder());
        }

        account = accountRepository.save(account);
        logger.info("Account updated: {} for user: {}", account.getName(), userId);

        return AccountDTO.fromEntity(account);
    }

    /**
     * 删除账户
     */
    @Transactional
    public void deleteAccount(Long userId, Long accountId) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("账户", accountId));

        if (account.getIsDefault()) {
            throw new BusinessException("默认账户不能删除");
        }

        accountRepository.delete(account);
        logger.info("Account deleted: {} for user: {}", accountId, userId);
    }

    /**
     * 获取单个账户
     */
    public AccountDTO getAccount(Long userId, Long accountId) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("账户", accountId));
        return AccountDTO.fromEntity(account);
    }

    /**
     * 获取账户实体
     */
    public Account getAccountEntity(Long userId, Long accountId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("账户", accountId));
    }

    /**
     * 更新账户余额
     */
    @Transactional
    public void updateBalance(Long accountId, BigDecimal amount, boolean isIncome) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("账户", accountId));

        if (isIncome) {
            account.setBalance(account.getBalance().add(amount));
        } else {
            account.setBalance(account.getBalance().subtract(amount));
        }

        accountRepository.save(account);
    }

    /**
     * 获取用户总余额
     */
    public BigDecimal getTotalBalance(Long userId) {
        return accountRepository.getTotalBalanceByUserId(userId);
    }
}
