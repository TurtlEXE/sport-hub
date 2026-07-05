package com.mvc.mock_project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "{profile.validation.currentPassword.required}")
    private String currentPassword;

    @NotBlank(message = "{profile.validation.newPassword.required}")
    @Size(min = 6, max = 100, message = "{profile.validation.newPassword.size}")
    private String newPassword;

    @NotBlank(message = "{profile.validation.confirmPassword.required}")
    private String confirmPassword;
}
