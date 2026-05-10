package com.accounting.repository;

import com.accounting.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 预算数据访问层
 */
@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserIdAndYearAndMonth(Long userId, Integer year, Integer month);

    List<Budget> findByUserIdAndYearAndMonthIsNull(Long userId, Integer year);

    Optional<Budget> findByIdAndUserId(Long id, Long userId);

    Optional<Budget> findByUserIdAndCategoryIdAndYearAndMonth(Long userId, Long categoryId, Integer year, Integer month);

    Optional<Budget> findByUserIdAndCategoryIsNullAndYearAndMonth(Long userId, Integer year, Integer month);

    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId AND b.year = :year " +
           "AND (b.month = :month OR b.month IS NULL) ORDER BY b.category.name")
    List<Budget> findBudgetsByUserIdAndPeriod(@Param("userId") Long userId, @Param("year") Integer year, @Param("month") Integer month);

    void deleteByUserIdAndCategoryId(Long userId, Long categoryId);
}
