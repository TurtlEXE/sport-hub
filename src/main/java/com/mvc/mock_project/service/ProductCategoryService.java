package com.mvc.mock_project.service;

import com.mvc.mock_project.dto.request.ProductCategoryRequest;
import com.mvc.mock_project.dto.response.ProductCategoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductCategoryService {
    Page<ProductCategoryDTO> getAllCategories(Pageable pageable);
    ProductCategoryDTO createCategory(ProductCategoryRequest request);
    ProductCategoryDTO updateCategory(Integer id, ProductCategoryRequest request);
    void toggleActiveStatus(Integer id);
}
