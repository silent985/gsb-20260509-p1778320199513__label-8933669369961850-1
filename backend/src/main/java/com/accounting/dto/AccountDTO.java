package com.accounting.dto;

import com.accounting.entity.Account;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {

    private Long id;

    @NotBlank(message = "账户名称不能为空")
    @Size(max = 50, message = "账户名称不能超过50个字符")
    private String name;

    @NotBlank(message = "账户类型不能为空")
    private String type;

    private BigDecimal balance;
    private String icon;
    private String color;
    private Boolean isDefault;
    private String note;
    private Integer sortOrder;
    private LocalDateTime createdAt;

    public static AccountDTO fromEntity(Account account) {
        return AccountDTO.builder()
                .id(account.getId())
                .name(account.getName())
                .type(account.getType())
                .balance(account.getBalance())
                .icon(account.getIcon())
                .color(account.getColor())
                .isDefault(account.getIsDefault())
                .note(account.getNote())
                .sortOrder(account.getSortOrder())
                .createdAt(account.getCreatedAt())
                .build();
    }
}
