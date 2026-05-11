package com.accounting.dto;

import com.accounting.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {

    private Long id;

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称不能超过50个字符")
    private String name;

    @NotBlank(message = "分类类型不能为空")
    private String type; // INCOME, EXPENSE

    private String icon;
    private String color;
    private Long parentId;
    private Integer sortOrder;
    private Boolean isSystem;
    private LocalDateTime createdAt;
    private List<CategoryDTO> children;

    public static CategoryDTO fromEntity(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .type(category.getType())
                .icon(category.getIcon())
                .color(category.getColor())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .sortOrder(category.getSortOrder())
                .isSystem(category.getIsSystem())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
