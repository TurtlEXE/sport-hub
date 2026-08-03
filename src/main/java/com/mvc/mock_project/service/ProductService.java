package com.mvc.mock_project.service;

import com.mvc.mock_project.dto.request.ProductFormDTO;
import com.mvc.mock_project.dto.response.ProductCategoryDTO;
import com.mvc.mock_project.dto.response.ProductDTO;

import java.util.List;

public interface ProductService {
    List<ProductDTO> getProductsByFacility(Integer facilityId, Integer ownerId);
    ProductDTO createProduct(Integer facilityId, Integer ownerId, ProductFormDTO dto);
    ProductDTO updateProduct(Integer productId, Integer facilityId, Integer ownerId, ProductFormDTO dto);
    void toggleProductStatus(Integer productId, Integer facilityId, Integer ownerId);
    List<ProductCategoryDTO> getActiveCategories();
}
