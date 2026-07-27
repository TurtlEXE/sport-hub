package com.mvc.mock_project.mapper;

import com.mvc.mock_project.dto.request.ProductCategoryRequest;
import com.mvc.mock_project.dto.response.ProductCategoryDTO;
import com.mvc.mock_project.entities.ProductCategory;
import org.springframework.stereotype.Component;

@Component
public class ProductCategoryMapper {

    public ProductCategory toEntity(ProductCategoryRequest request) {
        if (request == null) return null;
        
        return ProductCategory.builder()
                .categoryCode(request.getCategoryCode())
                .categoryName(request.getCategoryName())
                .isActive(true)
                .build();
    }

    public void updateEntityFromRequest(ProductCategory category, ProductCategoryRequest request) {
        if (request == null || category == null) return;
        
        category.setCategoryName(request.getCategoryName());
    }

    public ProductCategoryDTO toDto(ProductCategory category) {
        if (category == null) return null;
        
        return ProductCategoryDTO.builder()
                .categoryId(category.getId())
                .categoryCode(category.getCategoryCode())
                .categoryName(category.getCategoryName())
                .isActive(category.getIsActive())
                .build();
    }
}
