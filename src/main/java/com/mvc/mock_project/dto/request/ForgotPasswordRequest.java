package com.mvc.mock_project.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.format}")
    private String email;
}
