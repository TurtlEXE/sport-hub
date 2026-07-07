package com.mvc.mock_project.dto.response.facility;

import lombok.Data;

import java.util.List;

@Data
public class CourtDTO {
    private Integer courtId;
    private String courtName;
    private String description;
    private Boolean isActive;
    private List<CourtAttributeDTO> attributes;
}
