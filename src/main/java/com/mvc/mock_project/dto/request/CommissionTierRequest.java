package com.mvc.mock_project.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionTierRequest {

    @NotNull(message = "Min price per minute is required")
    @DecimalMin(value = "0.0", message = "Min price per minute must be non-negative")
    private BigDecimal minPricePerMinute;

    private BigDecimal maxPricePerMinute;

    @NotNull(message = "Commission rate is required")
    @DecimalMin(value = "0.0", message = "Commission rate must be non-negative")
    @DecimalMax(value = "1.0", message = "Commission rate must not exceed 1.0")
    private BigDecimal commissionRate;

    @NotNull(message = "Effective from date is required")
    private LocalDateTime effectiveFrom;

    private LocalDateTime effectiveTo;

    private Integer noticeDays;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
