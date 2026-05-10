package com.accounting.service;

import com.accounting.dto.CategoryDTO;
import com.accounting.entity.Category;
import com.accounting.entity.User;
import com.accounting.exception.BusinessException;
import com.accounting.exception.ResourceNotFoundException;
import com.accounting.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务
 */
@Service
public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * 初始化默认分类
     */
    @Transactional
    public void initDefaultCategories(User user) {
        // 支出分类
        String[][] expenseCategories = {
            {"餐饮", "🍜", "#ff4d4f"},
            {"交通", "🚗", "#1890ff"},
            {"购物", "🛒", "#fa8c16"},
            {"娱乐", "🎮", "#722ed1"},
            {"居住", "🏠", "#13c2c2"},
            {"医疗", "💊", "#eb2f96"},
            {"教育", "📚", "#2f54eb"},
            {"通讯", "📱", "#52c41a"},
            {"其他", "📌", "#8c8c8c"}
        };

        int sortOrder = 0;
        for (String[] cat : expenseCategories) {
            Category category = Category.builder()
                    .user(user)
                    .name(cat[0])
                    .type("EXPENSE")
                    .icon(cat[1])
                    .color(cat[2])
                    .sortOrder(sortOrder++)
                    .isSystem(true)
                    .build();
            categoryRepository.save(category);
        }

        // 收入分类
        String[][] incomeCategories = {
            {"工资", "💰", "#52c41a"},
            {"奖金", "🎁", "#faad14"},
            {"投资", "📈", "#1890ff"},
            {"兼职", "💼", "#722ed1"},
            {"其他", "📌", "#8c8c8c"}
        };

        sortOrder = 0;
        for (String[] cat : incomeCategories) {
            Category category = Category.builder()
                    .user(user)
                    .name(cat[0])
                    .type("INCOME")
                    .icon(cat[1])
                    .color(cat[2])
                    .sortOrder(sortOrder++)
                    .isSystem(true)
                    .build();
            categoryRepository.save(category);
        }

        logger.info("Default categories initialized for user: {}", user.getUsername());
    }

    /**
     * 获取用户所有分类
     */
    @Transactional(readOnly = true)
    public List<CategoryDTO> getUserCategories(Long userId) {
        return categoryRepository.findByUserIdAndParentIsNullOrderBySortOrderAsc(userId)
                .stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 按类型获取分类
     */
    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategoriesByType(Long userId, String type) {
        return categoryRepository.findRootCategoriesByUserIdAndType(userId, type)
                .stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 创建分类
     */
    @Transactional
    public CategoryDTO createCategory(Long userId, CategoryDTO dto, User user) {
        // 检查分类名是否重复
        if (categoryRepository.existsByNameAndUserIdAndType(dto.getName(), userId, dto.getType())) {
            throw new BusinessException("同类型下分类名称已存在");
        }

        Category parent = null;
        if (dto.getParentId() != null) {
            parent = categoryRepository.findByIdAndUserId(dto.getParentId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("父分类", dto.getParentId()));
        }

        Category category = Category.builder()
                .user(user)
                .name(dto.getName())
                .type(dto.getType())
                .icon(dto.getIcon())
                .color(dto.getColor())
                .parent(parent)
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .isSystem(false)
                .build();

        category = categoryRepository.save(category);
        logger.info("Category created: {} for user: {}", category.getName(), userId);

        return CategoryDTO.fromEntity(category);
    }

    /**
     * 更新分类
     */
    @Transactional
    public CategoryDTO updateCategory(Long userId, Long categoryId, CategoryDTO dto) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("分类", categoryId));

        category.setName(dto.getName());
        category.setIcon(dto.getIcon());
        category.setColor(dto.getColor());
        if (dto.getSortOrder() != null) {
            category.setSortOrder(dto.getSortOrder());
        }

        category = categoryRepository.save(category);
        logger.info("Category updated: {} for user: {}", category.getName(), userId);

        return CategoryDTO.fromEntity(category);
    }

    /**
     * 删除分类
     */
    @Transactional
    public void deleteCategory(Long userId, Long categoryId) {
        Category category = categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("分类", categoryId));

        if (category.getIsSystem()) {
            throw new BusinessException("系统分类不能删除");
        }

        categoryRepository.delete(category);
        logger.info("Category deleted: {} for user: {}", categoryId, userId);
    }

    /**
     * 获取分类实体
     */
    public Category getCategoryEntity(Long userId, Long categoryId) {
        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("分类", categoryId));
    }
}
