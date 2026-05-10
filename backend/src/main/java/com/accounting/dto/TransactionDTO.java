package com.accounting.dto;

import com.accounting.entity.Transaction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 交易记录DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {

    private Long id;

    @NotNull(message = "账户ID不能为空")
    private Long accountId;
    private String accountName;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;

    @NotNull(message = "交易类型不能为空")
    private String type; // INCOME, EXPENSE

    @NotNull(message = "金额不能为空")
    @Positive(message = "金额必须大于0")
    private BigDecimal amount;

    @NotNull(message = "交易日期不能为空")
    private LocalDate transactionDate;

    private String note;
    private String tags;
    private String attachment;
    private LocalDateTime createdAt;

    public static TransactionDTO fromEntity(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .accountId(transaction.getAccount().getId())
                .accountName(transaction.getAccount().getName())
                .categoryId(transaction.getCategory().getId())
                .categoryName(transaction.getCategory().getName())
                .categoryIcon(transaction.getCategory().getIcon())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .transactionDate(transaction.getTransactionDate())
                .note(transaction.getNote())
                .tags(transaction.getTags())
                .attachment(transaction.getAttachment())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
