package com.mvc.mock_project.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionPolicyRequest {

    @NotNull(message = "Min notice days is required")
    @Min(value = 1, message = "Min notice days must be at least 1")
    @Max(value = 90, message = "Min notice days must be at most 90")
    private Integer minNoticeDays;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
