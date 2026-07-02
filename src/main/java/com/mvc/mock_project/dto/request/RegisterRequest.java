package com.mvc.mock_project.dto.request;

import com.mvc.mock_project.entities.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class RegisterRequest {

    @NotBlank(message = "{validation.fullname.required}")
    @Size(max = 255, message = "{validation.fullname.size}")
    private String fullName;

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.format}")
    private String email;

    @NotBlank(message = "{validation.password.required}")
    @Size(min = 8, max = 100, message = "{validation.password.size}")
    private String password;

    @NotBlank(message = "{validation.confirm_password.required}")
    private String confirmPassword;

    @NotBlank(message = "{validation.phone.required}")
    @Pattern(regexp = "^(0[3|5|7|8|9])+([0-9]{8})$", message = "{validation.phone.format}")
    private String phone;

}
