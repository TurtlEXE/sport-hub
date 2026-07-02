package com.mvc.mock_project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CompleteProfileRequest {

    @NotBlank(message = "{validation.phone.required}")
    @Pattern(regexp = "^(0[3|5|7|8|9])+([0-9]{8})$", message = "{validation.phone.format}")
    private String phone;
}
