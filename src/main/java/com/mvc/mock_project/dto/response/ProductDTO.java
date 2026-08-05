package com.mvc.mock_project.dto.response;

import com.mvc.mock_project.entities.enums.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Integer id;
    private Integer facilityId;
    private Integer categoryId;
    private String categoryName;
    private String categoryCode;
    private String productName;
    private String description;
    private String imagePath;
    private ProductType productType;
    private BigDecimal price;
    private String rentalUnit;
    private Integer stockQuantity;
    private Boolean isActive;
    private Boolean hasOrders;
    private LocalDateTime createdAt;
}
