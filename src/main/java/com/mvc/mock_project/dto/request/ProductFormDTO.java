package com.mvc.mock_project.dto.request;

import com.mvc.mock_project.entities.enums.ProductType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductFormDTO {

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String productName;

    @NotNull(message = "Danh mục không được để trống")
    private Integer categoryId;

    @NotNull(message = "Loại sản phẩm không được để trống")
    private ProductType productType;

    @NotNull(message = "Giá không được để trống")
    @Min(value = 0, message = "Giá không được âm")
    private BigDecimal price;

    @NotBlank(message = "Đơn vị tính không được để trống")
    private String rentalUnit; // Used for both SALE and RENTAL as unit of measurement

    @Min(value = 0, message = "Tồn kho không được âm")
    private Integer stockQuantity; // Optional (null = unlimited)

    private String description;

    private Boolean isActive;

    private MultipartFile imageFile; // For image upload
}
