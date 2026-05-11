package com.accounting.controller;

import com.accounting.dto.ApiResponse;
import com.accounting.dto.CategoryDTO;
import com.accounting.service.AuthService;
import com.accounting.service.CategoryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AuthService authService;

    /**
     * 获取所有分类
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getCategories(
            @RequestParam(required = false) String type) {
        Long userId = authService.getCurrentUserId();
        List<CategoryDTO> categories;
        if (type != null && !type.isEmpty()) {
            categories = categoryService.getCategoriesByType(userId, type);
        } else {
            categories = categoryService.getUserCategories(userId);
        }
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    /**
     * 创建分类
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(@Valid @RequestBody CategoryDTO dto) {
        Long userId = authService.getCurrentUserId();
        CategoryDTO category = categoryService.createCategory(userId, dto, authService.getCurrentUser());
        return ResponseEntity.ok(ApiResponse.success("分类创建成功", category));
    }

    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategory(
            @PathVariable Long id, @Valid @RequestBody CategoryDTO dto) {
        Long userId = authService.getCurrentUserId();
        CategoryDTO category = categoryService.updateCategory(userId, id, dto);
        return ResponseEntity.ok(ApiResponse.success("分类更新成功", category));
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        Long userId = authService.getCurrentUserId();
        categoryService.deleteCategory(userId, id);
        return ResponseEntity.ok(ApiResponse.success("分类删除成功", null));
    }
}
