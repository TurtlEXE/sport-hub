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

    @NotBlank(message = "{auth.register.fullname}")
    @Size(max = 255)
    private String fullName;

    @NotBlank(message = "{auth.register.email}")
    @Email(message = "{auth.error.invalid_credentials}")
    private String email;

    @NotBlank(message = "{auth.register.password}")
    @Size(min = 8, max = 100)
    private String password;

    @NotBlank(message = "{auth.register.confirm_password}")
    private String confirmPassword;

    @NotBlank(message = "{auth.register.phone}")
    @Pattern(regexp = "^(0[3|5|7|8|9])+([0-9]{8})$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @NotNull
    private Role role;
}
