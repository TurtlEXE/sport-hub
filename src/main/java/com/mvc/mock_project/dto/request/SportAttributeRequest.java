package com.mvc.mock_project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SportAttributeRequest {

    @NotBlank(message = "Attribute code is required")
    @Pattern(regexp = "^[a-z_]{2,50}$", message = "Attribute code must be lowercase letters or underscores, 2-50 characters")
    private String attributeCode;

    @NotBlank(message = "Attribute name is required")
    @Size(max = 100, message = "Attribute name must not exceed 100 characters")
    private String attributeName;

    @NotBlank(message = "Data type is required")
    @Pattern(regexp = "^(TEXT|NUMBER|BOOLEAN|SELECT)$", message = "Data type must be TEXT, NUMBER, BOOLEAN, or SELECT")
    private String dataType;

    private String optionsJson;

    private Boolean isRequired;
}
