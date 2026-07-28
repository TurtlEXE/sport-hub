package com.mvc.mock_project.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SportRequest {

    @NotBlank(message = "Sport code is required")
    @Pattern(regexp = "^[A-Z_]{2,30}$", message = "Sport code must be uppercase letters or underscores, 2-30 characters")
    private String sportCode;

    @NotBlank(message = "Sport name is required")
    @Size(max = 100, message = "Sport name must not exceed 100 characters")
    private String sportName;

    private String iconPath;

    @NotNull(message = "Default min duration is required")
    @Min(value = 15, message = "Min duration must be at least 15 minutes")
    @Max(value = 480, message = "Min duration must be at most 480 minutes")
    private Integer defaultMinDurationMinutes;

    @NotNull(message = "Default slot step is required")
    @Min(value = 15, message = "Slot step must be at least 15 minutes")
    @Max(value = 120, message = "Slot step must be at most 120 minutes")
    private Integer defaultSlotStepMinutes;

    @Valid
    private List<SportAttributeRequest> attributes;
}
