package com.mvc.mock_project.dto.response.facility;

import lombok.Data;

@Data
public class FacilityImageDTO {
    private Integer imageId;
    private String imageUrl;
    private Boolean isThumbnail;
}
