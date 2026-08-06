package com.mvc.mock_project.service.impl;

import com.mvc.mock_project.dto.request.ProductFormDTO;
import com.mvc.mock_project.dto.response.ProductCategoryDTO;
import com.mvc.mock_project.dto.response.ProductDTO;
import com.mvc.mock_project.entities.Facility;
import com.mvc.mock_project.entities.Product;
import com.mvc.mock_project.entities.ProductCategory;
import com.mvc.mock_project.entities.enums.ProductType;
import com.mvc.mock_project.repository.FacilityRepository;
import com.mvc.mock_project.repository.OrderItemRepository;
import com.mvc.mock_project.repository.ProductCategoryRepository;
import com.mvc.mock_project.repository.ProductRepository;
import com.mvc.mock_project.service.CloudinaryService;
import com.mvc.mock_project.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final FacilityRepository facilityRepository;
    private final OrderItemRepository orderItemRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> getProductsByFacility(Integer facilityId, Integer ownerId, String search, Integer categoryId, ProductType type, Boolean status, Pageable pageable) {
        // Validate ownership
        Facility facility = facilityRepository.findByIdAndOwner_Id(facilityId, ownerId)
                .orElseThrow(() -> new RuntimeException("Facility not found or unauthorized"));

        Page<Product> products = productRepository.findFilteredProducts(facilityId, search, categoryId, type, status, pageable);
        
        return products.map(this::mapToDTO);
    }

    @Override
    @Transactional
    public ProductDTO createProduct(Integer facilityId, Integer ownerId, ProductFormDTO dto) {
        Facility facility = facilityRepository.findByIdAndOwner_Id(facilityId, ownerId)
                .orElseThrow(() -> new RuntimeException("Facility not found or unauthorized"));

        ProductCategory category = productCategoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Product product = Product.builder()
                .facility(facility)
                .category(category)
                .productName(dto.getProductName())
                .description(dto.getDescription())
                .productType(dto.getProductType())
                .price(dto.getPrice())
                .rentalUnit(dto.getRentalUnit())
                .stockQuantity(dto.getStockQuantity())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();

        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            String imageUrl = cloudinaryService.uploadProductImage(dto.getImageFile(), facilityId);
            product.setImagePath(imageUrl);
        }

        Product savedProduct = productRepository.save(product);
        return mapToDTO(savedProduct);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Integer productId, Integer facilityId, Integer ownerId, ProductFormDTO dto) {
        Facility facility = facilityRepository.findByIdAndOwner_Id(facilityId, ownerId)
                .orElseThrow(() -> new RuntimeException("Facility not found or unauthorized"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getFacility().getId().equals(facilityId)) {
            throw new RuntimeException("Product does not belong to this facility");
        }

        ProductCategory category = productCategoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        product.setProductName(dto.getProductName());
        product.setCategory(category);
        product.setProductType(dto.getProductType());
        product.setPrice(dto.getPrice());
        product.setRentalUnit(dto.getRentalUnit());
        product.setStockQuantity(dto.getStockQuantity());
        product.setDescription(dto.getDescription());
        product.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);

        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            // Delete old image if exists
            if (product.getImagePath() != null) {
                cloudinaryService.deleteImage(product.getImagePath());
            }
            String imageUrl = cloudinaryService.uploadProductImage(dto.getImageFile(), facilityId);
            product.setImagePath(imageUrl);
        }

        Product updatedProduct = productRepository.save(product);
        return mapToDTO(updatedProduct);
    }

    @Override
    @Transactional
    public void toggleProductStatus(Integer productId, Integer facilityId, Integer ownerId) {
        Facility facility = facilityRepository.findByIdAndOwner_Id(facilityId, ownerId)
                .orElseThrow(() -> new RuntimeException("Facility not found or unauthorized"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getFacility().getId().equals(facilityId)) {
            throw new RuntimeException("Product does not belong to this facility");
        }

        product.setIsActive(!product.getIsActive());
        productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Integer productId, Integer facilityId, Integer ownerId) {
        Facility facility = facilityRepository.findByIdAndOwner_Id(facilityId, ownerId)
                .orElseThrow(() -> new RuntimeException("Facility not found or unauthorized"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!product.getFacility().getId().equals(facilityId)) {
            throw new RuntimeException("Product does not belong to this facility");
        }

        boolean hasOrders = orderItemRepository.existsByProductId(productId);
        
        if (hasOrders) {
            // Soft delete
            product.setIsActive(false);
            productRepository.save(product);
        } else {
            // Hard delete
            if (product.getImagePath() != null) {
                cloudinaryService.deleteImage(product.getImagePath());
            }
            productRepository.delete(product);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductCategoryDTO> getActiveCategories() {
        return productCategoryRepository.findByIsActiveTrue().stream()
                .map(cat -> ProductCategoryDTO.builder()
                        .categoryId(cat.getId())
                        .categoryCode(cat.getCategoryCode())
                        .categoryName(cat.getCategoryName())
                        .isActive(cat.getIsActive())
                        .build())
                .collect(Collectors.toList());
    }

    private ProductDTO mapToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .facilityId(product.getFacility().getId())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getCategoryName())
                .categoryCode(product.getCategory().getCategoryCode())
                .productName(product.getProductName())
                .description(product.getDescription())
                .imagePath(product.getImagePath())
                .productType(product.getProductType())
                .price(product.getPrice())
                .rentalUnit(product.getRentalUnit())
                .stockQuantity(product.getStockQuantity())
                .isActive(product.getIsActive())
                .hasOrders(orderItemRepository.existsByProductId(product.getId()))
                .createdAt(product.getCreatedAt())
                .build();
    }
}
