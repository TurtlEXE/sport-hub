package com.mvc.mock_project.service.impl;

import com.mvc.mock_project.dto.request.ProductCategoryRequest;
import com.mvc.mock_project.dto.response.ProductCategoryDTO;
import com.mvc.mock_project.entities.ProductCategory;
import com.mvc.mock_project.exception.DuplicateCodeException;
import com.mvc.mock_project.mapper.ProductCategoryMapper;
import com.mvc.mock_project.repository.ProductCategoryRepository;
import com.mvc.mock_project.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;
    private final ProductCategoryMapper productCategoryMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductCategoryDTO> getAllCategories(Pageable pageable) {
        return productCategoryRepository.findAll(pageable)
                .map(productCategoryMapper::toDto);
    }

    @Override
    @Transactional
    public ProductCategoryDTO createCategory(ProductCategoryRequest request) {
        if (productCategoryRepository.existsByCategoryCode(request.getCategoryCode())) {
            throw new DuplicateCodeException("Product category code already exists: " + request.getCategoryCode());
        }

        ProductCategory category = productCategoryMapper.toEntity(request);
        ProductCategory savedCategory = productCategoryRepository.save(category);
        log.info("Created new product category: {}", savedCategory.getCategoryCode());
        return productCategoryMapper.toDto(savedCategory);
    }

    @Override
    @Transactional
    public ProductCategoryDTO updateCategory(Integer id, ProductCategoryRequest request) {
        ProductCategory category = productCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product Category not found with ID: " + id));

        if (!category.getCategoryCode().equals(request.getCategoryCode())) {
            // Check if new code exists
            if (productCategoryRepository.existsByCategoryCode(request.getCategoryCode())) {
                throw new DuplicateCodeException("Product category code already exists: " + request.getCategoryCode());
            }
            category.setCategoryCode(request.getCategoryCode());
        }

        productCategoryMapper.updateEntityFromRequest(category, request);
        ProductCategory savedCategory = productCategoryRepository.save(category);
        log.info("Updated product category: {}", savedCategory.getCategoryCode());
        return productCategoryMapper.toDto(savedCategory);
    }

    @Override
    @Transactional
    public void toggleActiveStatus(Integer id) {
        ProductCategory category = productCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product Category not found with ID: " + id));

        category.setIsActive(!category.getIsActive());
        productCategoryRepository.save(category);
        log.info("Toggled active status for product category {} to {}", category.getCategoryCode(), category.getIsActive());
    }
}
