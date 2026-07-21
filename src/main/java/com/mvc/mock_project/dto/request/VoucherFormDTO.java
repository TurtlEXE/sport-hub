package com.mvc.mock_project.dto.request;

import com.mvc.mock_project.entities.enums.DiscountType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherFormDTO {

    private Integer id;

    @NotBlank(message = "Voucher code is required")
    private String code;

    @NotBlank(message = "Voucher name is required")
    private String name;

    private String description;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @Min(value = 0, message = "Discount value must be positive")
    private BigDecimal discountValue;

    @NotNull(message = "Active time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime validFrom;

    @NotNull(message = "Disable time is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime validTo;

    @Min(value = 0, message = "Minimum order amount must be positive")
    private BigDecimal minOrderAmount;

    @Min(value = 0, message = "Maximum discount amount must be positive")
    private BigDecimal maxDiscountAmount;

    @Min(value = 1, message = "Usage limit must be at least 1")
    private Integer usageLimit;

    @Min(value = 1, message = "Per user limit must be at least 1")
    private Integer perUserLimit;

    @Builder.Default
    private Boolean isActive = true;

    private java.util.List<Integer> facilityIds;
}
