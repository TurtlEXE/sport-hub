package com.mvc.mock_project.service;

import com.mvc.mock_project.dto.request.ChangePasswordRequest;
import com.mvc.mock_project.dto.request.UpdateOwnerProfileRequest;
import com.mvc.mock_project.dto.request.UpdateProfileRequest;
import com.mvc.mock_project.dto.response.ProfileResponse;

public interface ProfileService {

    /**
     * Get profile data for the given account.
     * Includes Owner-specific fields if the account role is OWNER.
     */
    ProfileResponse getProfile(Integer accountId);

    /**
     * Update common profile fields (fullName, phone).
     */
    void updateProfile(Integer accountId, UpdateProfileRequest request);

    /**
     * Update Owner-specific profile fields (business info, bank details).
     * Only allowed for accounts with role = OWNER.
     */
    void updateOwnerProfile(Integer accountId, UpdateOwnerProfileRequest request);

    /**
     * Change the account password.
     * Requires the current password for verification.
     */
    void changePassword(Integer accountId, ChangePasswordRequest request);

    /**
     * Upload avatar to Cloudinary and update the avatar path for the given account.
     */
    String uploadAvatar(Integer accountId, org.springframework.web.multipart.MultipartFile file);
}
