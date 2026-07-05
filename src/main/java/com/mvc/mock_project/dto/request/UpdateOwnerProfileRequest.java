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
public class UpdateOwnerProfileRequest {

    @NotBlank(message = "{profile.validation.fullName.required}")
    @Size(min = 2, max = 255, message = "{profile.validation.fullName.size}")
    private String fullName;

    @Pattern(regexp = "^(\\+?\\d{9,15})?$", message = "{profile.validation.phone.invalid}")
    private String phone;

    @NotBlank(message = "{profile.validation.businessName.required}")
    @Size(max = 255, message = "{profile.validation.businessName.size}")
    private String businessName;

    @Size(max = 50, message = "{profile.validation.taxCode.size}")
    private String taxCode;

    @Size(max = 100, message = "{profile.validation.bankName.size}")
    private String bankName;

    @Size(max = 50, message = "{profile.validation.bankAccountNo.size}")
    private String bankAccountNo;

    @Size(max = 255, message = "{profile.validation.bankAccountName.size}")
    private String bankAccountName;
}
