package com.mvc.mock_project.controller.api.profile;

import com.mvc.mock_project.dto.request.ChangePasswordRequest;
import com.mvc.mock_project.dto.request.UpdateOwnerProfileRequest;
import com.mvc.mock_project.dto.request.UpdateProfileRequest;
import com.mvc.mock_project.dto.response.ApiResponse;
import com.mvc.mock_project.dto.response.ProfileResponse;
import com.mvc.mock_project.entities.Account;
import com.mvc.mock_project.security.CustomOAuth2User;
import com.mvc.mock_project.security.CustomUserDetails;
import com.mvc.mock_project.service.CloudinaryService;
import com.mvc.mock_project.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileApiController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(Authentication authentication) {
        Integer accountId = extractAccountId(authentication);
        ProfileResponse profile = profileService.getProfile(accountId);
        return ResponseEntity.ok(ApiResponse.success("OK", profile));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        Integer accountId = extractAccountId(authentication);
        profileService.updateProfile(accountId, request);
        return ResponseEntity.ok(ApiResponse.success("msg.success.profile_updated"));
    }

    @PutMapping("/owner")
    public ResponseEntity<ApiResponse<Void>> updateOwnerProfile(
            @Valid @RequestBody UpdateOwnerProfileRequest request,
            Authentication authentication) {
        Integer accountId = extractAccountId(authentication);
        profileService.updateOwnerProfile(accountId, request);
        return ResponseEntity.ok(ApiResponse.success("msg.success.profile_updated"));
    }

    @PostMapping("/avatar")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        Integer accountId = extractAccountId(authentication);
        String avatarUrl = profileService.uploadAvatar(accountId, file);
        return ResponseEntity.ok(ApiResponse.success("msg.success.avatar_updated", avatarUrl));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        Integer accountId = extractAccountId(authentication);
        profileService.changePassword(accountId, request);
        return ResponseEntity.ok(ApiResponse.success("msg.success.password_changed"));
    }

    private Integer extractAccountId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getAccount().getId();
        } else if (principal instanceof CustomOAuth2User) {
            return ((CustomOAuth2User) principal).getAccount().getId();
        }
        throw new RuntimeException("msg.error.account_not_found");
    }
}
