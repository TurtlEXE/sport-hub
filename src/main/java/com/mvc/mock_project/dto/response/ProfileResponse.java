package com.mvc.mock_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

    // Common fields (all roles)
    private Integer accountId;
    private String email;
    private String fullName;
    private String phone;
    private String avatarPath;
    private String role;

    // Owner-specific fields (only populated when role = OWNER)
    private String businessName;
    private String taxCode;
    private String bankName;
    private String bankAccountNo;
    private String bankAccountName;
    private String approvalStatus;

    // Flag to indicate if user logged in via OAuth2 (no password to change)
    private boolean oauthUser;
}
