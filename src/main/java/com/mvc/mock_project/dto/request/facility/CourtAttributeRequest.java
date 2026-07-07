package com.mvc.mock_project.dto.request.facility;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CourtAttributeRequest {
    @NotNull
    private Integer attributeId;
    
    @NotBlank
    private String value;
}
