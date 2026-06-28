package com.mvc.mock_project.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OwnerRegisterRequest extends RegisterRequest {

    @NotBlank(message = "{auth.register_owner.business_name}")
    private String businessName;

    private String taxCode;

    private String bankName;

    private String bankAccountNo;

    private String bankAccountName;
}
