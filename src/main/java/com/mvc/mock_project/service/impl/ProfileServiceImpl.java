package com.mvc.mock_project.service.impl;

import com.mvc.mock_project.dto.request.ChangePasswordRequest;
import com.mvc.mock_project.dto.request.UpdateOwnerProfileRequest;
import com.mvc.mock_project.dto.request.UpdateProfileRequest;
import com.mvc.mock_project.dto.response.ProfileResponse;
import com.mvc.mock_project.entities.Account;
import com.mvc.mock_project.entities.OwnerProfile;
import com.mvc.mock_project.entities.enums.Role;
import com.mvc.mock_project.repository.AccountRepository;
import com.mvc.mock_project.repository.OwnerProfileRepository;
import com.mvc.mock_project.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final AccountRepository accountRepository;
    private final OwnerProfileRepository ownerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.mvc.mock_project.service.CloudinaryService cloudinaryService;

    @Override
    public ProfileResponse getProfile(Integer accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("msg.error.account_not_found"));

        ProfileResponse.ProfileResponseBuilder builder = ProfileResponse.builder()
                .accountId(account.getId())
                .email(account.getEmail())
                .fullName(account.getFullName())
                .phone(account.getPhone())
                .avatarPath(account.getAvatarPath())
                .role(account.getRole().name())
                .oauthUser(account.getGoogleId() != null && !account.getGoogleId().isBlank());

        // Load Owner-specific fields
        if (account.getRole() == Role.OWNER) {
            ownerProfileRepository.findByAccountId(accountId).ifPresent(ownerProfile -> {
                builder.businessName(ownerProfile.getBusinessName())
                        .taxCode(ownerProfile.getTaxCode())
                        .bankName(ownerProfile.getBankName())
                        .bankAccountNo(ownerProfile.getBankAccountNo())
                        .bankAccountName(ownerProfile.getBankAccountName())
                        .approvalStatus(ownerProfile.getApprovalStatus() != null
                                ? ownerProfile.getApprovalStatus().name() : null);
            });
        }

        return builder.build();
    }

    @Override
    @Transactional
    public void updateProfile(Integer accountId, UpdateProfileRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("msg.error.account_not_found"));

        // Validate phone uniqueness (exclude current user)
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            if (accountRepository.existsByPhoneAndIdNot(request.getPhone(), accountId)) {
                throw new RuntimeException("msg.error.phone_exists");
            }
            account.setPhone(request.getPhone());
        }

        account.setFullName(request.getFullName());
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void updateOwnerProfile(Integer accountId, UpdateOwnerProfileRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("msg.error.account_not_found"));

        if (account.getRole() != Role.OWNER) {
            throw new RuntimeException("msg.error.not_owner");
        }

        // Update common fields
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            if (accountRepository.existsByPhoneAndIdNot(request.getPhone(), accountId)) {
                throw new RuntimeException("msg.error.phone_exists");
            }
            account.setPhone(request.getPhone());
        }
        account.setFullName(request.getFullName());
        accountRepository.save(account);

        // Update Owner-specific fields
        OwnerProfile ownerProfile = ownerProfileRepository.findByAccountId(accountId)
                .orElseThrow(() -> new RuntimeException("msg.error.owner_profile_not_found"));

        ownerProfile.setBusinessName(request.getBusinessName());
        ownerProfile.setTaxCode(request.getTaxCode());
        ownerProfile.setBankName(request.getBankName());
        ownerProfile.setBankAccountNo(request.getBankAccountNo());
        ownerProfile.setBankAccountName(request.getBankAccountName());
        ownerProfileRepository.save(ownerProfile);
    }

    @Override
    @Transactional
    public void changePassword(Integer accountId, ChangePasswordRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("msg.error.account_not_found"));

        // OAuth2 users cannot change password
        if (account.getGoogleId() != null && !account.getGoogleId().isBlank()
                && (account.getPasswordHash() == null || account.getPasswordHash().isBlank())) {
            throw new RuntimeException("msg.error.oauth_no_password");
        }

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), account.getPasswordHash())) {
            throw new RuntimeException("msg.error.wrong_current_password");
        }

        // Verify new passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("msg.error.passwords_mismatch");
        }

        account.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public String uploadAvatar(Integer accountId, org.springframework.web.multipart.MultipartFile file) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("msg.error.account_not_found"));

        // Upload new avatar directly. Cloudinary will overwrite the existing file
        // with the same public_id ("avatar_" + accountId)
        String avatarUrl = cloudinaryService.uploadAvatar(file, accountId);

        account.setAvatarPath(avatarUrl);
        accountRepository.save(account);
        
        return avatarUrl;
    }
}
