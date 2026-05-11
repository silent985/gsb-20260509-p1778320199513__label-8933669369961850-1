package com.accounting.repository;

import com.accounting.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 分类数据访问层
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUserIdAndTypeOrderBySortOrderAsc(Long userId, String type);

    List<Category> findByUserIdOrderBySortOrderAsc(Long userId);

    List<Category> findByUserIdAndParentIsNullOrderBySortOrderAsc(Long userId);

    Optional<Category> findByIdAndUserId(Long id, Long userId);

    boolean existsByNameAndUserIdAndType(String name, Long userId, String type);

    @Query("SELECT c FROM Category c WHERE c.user.id = :userId AND c.parent IS NULL AND c.type = :type ORDER BY c.sortOrder")
    List<Category> findRootCategoriesByUserIdAndType(@Param("userId") Long userId, @Param("type") String type);
}
