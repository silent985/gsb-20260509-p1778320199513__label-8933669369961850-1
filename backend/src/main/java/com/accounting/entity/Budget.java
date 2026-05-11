package com.accounting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预算实体类
 * 支持月度和年度预算设置
 */
@Entity
@Table(name = "budgets", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "category_id", "year", "month"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "spent_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal spentAmount = BigDecimal.ZERO;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String period = "MONTHLY"; // MONTHLY, YEARLY

    @Column(nullable = false)
    private Integer year;

    @Column
    private Integer month; // null for yearly budgets

    @Column(name = "alert_threshold")
    @Builder.Default
    private Integer alertThreshold = 80; // 80% 预警阈值

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * 计算预算使用百分比
     */
    @Transient
    public BigDecimal getUsagePercentage() {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return spentAmount.multiply(BigDecimal.valueOf(100)).divide(amount, 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * 检查是否超出预算
     */
    @Transient
    public boolean isOverBudget() {
        return spentAmount.compareTo(amount) > 0;
    }

    /**
     * 检查是否达到预警阈值
     */
    @Transient
    public boolean isAlertTriggered() {
        return getUsagePercentage().compareTo(BigDecimal.valueOf(alertThreshold)) >= 0;
    }
}
