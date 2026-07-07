package com.mvc.mock_project.dto.request.facility;

import lombok.Data;
import java.util.List;

@Data
public class UpdateCourtRequest {
    private String courtName;
    private String description;
    private List<CourtAttributeRequest> attributes;
}
