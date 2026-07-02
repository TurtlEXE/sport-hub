package com.mvc.mock_project.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerifyOtpRequest {

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.format}")
    private String email;

    @NotBlank(message = "{validation.otp.required}")
    @Size(min = 6, max = 6, message = "{validation.otp.size}")
    private String otp;
}
