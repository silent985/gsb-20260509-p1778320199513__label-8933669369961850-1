package com.accounting.repository;

import com.accounting.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 交易记录数据访问层
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByUserIdOrderByTransactionDateDescCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    List<Transaction> findByUserIdAndTransactionDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    List<Transaction> findByUserIdAndTypeAndTransactionDateBetween(Long userId, String type, LocalDate startDate, LocalDate endDate);

    // 总收入
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = 'INCOME'")
    BigDecimal getTotalIncomeByUserId(@Param("userId") Long userId);

    // 总支出
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = 'EXPENSE'")
    BigDecimal getTotalExpenseByUserId(@Param("userId") Long userId);

    // 日期范围内收入
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = 'INCOME' AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getIncomeByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 日期范围内支出
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId AND t.type = 'EXPENSE' AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getExpenseByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 按分类统计支出
    @Query("SELECT t.category.id, t.category.name, t.category.icon, t.category.color, SUM(t.amount), COUNT(t) " +
           "FROM Transaction t WHERE t.user.id = :userId AND t.type = :type " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY t.category.id, t.category.name, t.category.icon, t.category.color " +
           "ORDER BY SUM(t.amount) DESC")
    List<Object[]> getCategoryStatsByUserIdAndTypeAndDateRange(
            @Param("userId") Long userId, 
            @Param("type") String type,
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);

    // 按月统计
    @Query("SELECT FUNCTION('DATE_FORMAT', t.transactionDate, '%Y-%m'), t.type, SUM(t.amount) " +
           "FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', t.transactionDate, '%Y-%m'), t.type " +
           "ORDER BY FUNCTION('DATE_FORMAT', t.transactionDate, '%Y-%m')")
    List<Object[]> getMonthlyTrendByUserId(
            @Param("userId") Long userId, 
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);

    // 按日统计
    @Query("SELECT t.transactionDate, t.type, SUM(t.amount) " +
           "FROM Transaction t WHERE t.user.id = :userId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY t.transactionDate, t.type " +
           "ORDER BY t.transactionDate")
    List<Object[]> getDailyTrendByUserId(
            @Param("userId") Long userId, 
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);

    // 统计某分类在指定月份的支出
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.category.id = :categoryId " +
           "AND t.type = 'EXPENSE' AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getCategoryExpenseByDateRange(
            @Param("userId") Long userId, 
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
}
