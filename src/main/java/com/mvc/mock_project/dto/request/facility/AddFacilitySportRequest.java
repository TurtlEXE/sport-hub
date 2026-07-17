package com.mvc.mock_project.dto.request.facility;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddFacilitySportRequest {
    @NotNull
    private Integer sportId;
    
    @NotNull
    @Min(30)
    private Integer minDurationMinutes;
    
    @NotNull
    @Min(30)
    private Integer slotStepMinutes;
}
