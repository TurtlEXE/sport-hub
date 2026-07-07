package com.mvc.mock_project.dto.response.facility;

import lombok.Data;

@Data
public class CourtAttributeDTO {
    private Integer attributeId;
    private String attributeCode;
    private String attributeName;
    private String dataType;
    private String optionsJson;
    private String value;
    private Boolean isRequired;
}
