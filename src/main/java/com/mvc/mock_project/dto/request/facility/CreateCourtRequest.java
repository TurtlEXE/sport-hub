package com.mvc.mock_project.dto.request.facility;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class CreateCourtRequest {
    @NotNull
    private Integer facilitySportId;
    
    @NotBlank
    private String courtName;
    
    private String description;
    
    private List<CourtAttributeRequest> attributes;
}
