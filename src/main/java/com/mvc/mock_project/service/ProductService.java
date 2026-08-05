package com.mvc.mock_project.service;

import com.mvc.mock_project.dto.request.ProductFormDTO;
import com.mvc.mock_project.dto.response.ProductCategoryDTO;
import com.mvc.mock_project.dto.response.ProductDTO;
import com.mvc.mock_project.entities.enums.ProductType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    Page<ProductDTO> getProductsByFacility(Integer facilityId, Integer ownerId, String search, Integer categoryId, ProductType type, Boolean status, Pageable pageable);
    ProductDTO createProduct(Integer facilityId, Integer ownerId, ProductFormDTO dto);
    ProductDTO updateProduct(Integer productId, Integer facilityId, Integer ownerId, ProductFormDTO dto);
    void toggleProductStatus(Integer productId, Integer facilityId, Integer ownerId);
    void deleteProduct(Integer productId, Integer facilityId, Integer ownerId);
    List<ProductCategoryDTO> getActiveCategories();
}
