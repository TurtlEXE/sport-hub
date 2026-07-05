package com.mvc.mock_project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "{profile.validation.fullName.required}")
    @Size(min = 2, max = 255, message = "{profile.validation.fullName.size}")
    private String fullName;

    @Pattern(regexp = "^(\\+?\\d{9,15})?$", message = "{profile.validation.phone.invalid}")
    private String phone;
}
