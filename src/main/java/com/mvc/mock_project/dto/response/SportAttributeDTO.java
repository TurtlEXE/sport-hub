package com.mvc.mock_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SportAttributeDTO {
    private Integer attributeId;
    private String attributeCode;
    private String attributeName;
    private String dataType;
    private String optionsJson;
    private Boolean isRequired;
    private Boolean isActive;
}
