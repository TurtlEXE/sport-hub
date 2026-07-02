package com.mvc.mock_project.service.impl;

import com.mvc.mock_project.dto.request.*;
import com.mvc.mock_project.dto.response.AuthResponse;
import com.mvc.mock_project.entities.Account;
import com.mvc.mock_project.entities.EmailVerification;
import com.mvc.mock_project.entities.OwnerProfile;
import com.mvc.mock_project.entities.PasswordResetToken;
import com.mvc.mock_project.entities.enums.ApprovalStatus;
import com.mvc.mock_project.entities.enums.Role;
import com.mvc.mock_project.exception.AccountNotActiveException;
import com.mvc.mock_project.exception.EmailAlreadyExistsException;
import com.mvc.mock_project.exception.InvalidOtpException;
import com.mvc.mock_project.exception.OtpExpiredException;
import com.mvc.mock_project.repository.AccountRepository;
import com.mvc.mock_project.repository.EmailVerificationRepository;
import com.mvc.mock_project.repository.OwnerProfileRepository;
import com.mvc.mock_project.repository.PasswordResetTokenRepository;
import com.mvc.mock_project.security.CustomUserDetails;
import com.mvc.mock_project.security.JwtTokenProvider;
import com.mvc.mock_project.service.AuthService;
import com.mvc.mock_project.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final OwnerProfileRepository ownerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Value("${app.otp.verify-email.expiration-minutes}")
    private int verifyOtpExpirationMinutes;

    @Value("${app.otp.reset-password.expiration-minutes}")
    private int resetOtpExpirationMinutes;

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    @Override
    @Transactional
    public void registerCustomer(RegisterRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("msg.error.email_exists");
        }

        if (accountRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("msg.error.phone_exists");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("msg.error.passwords_mismatch");
        }

        emailVerificationRepository.deleteByEmail(request.getEmail());

        Account account = accountRepository.findByEmail(request.getEmail()).orElse(new Account());
        account.setEmail(request.getEmail());
        account.setFullName(request.getFullName());
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setPhone(request.getPhone());
        account.setRole(Role.CUSTOMER);
        account.setIsActive(false);
        account.setCreatedAt(LocalDateTime.now());
        accountRepository.save(account);

        String otp = generateOtp();
        EmailVerification verifyOtp = new EmailVerification();
        verifyOtp.setEmail(request.getEmail());
        verifyOtp.setToken(otp);
        verifyOtp.setExpireAt(LocalDateTime.now().plusMinutes(verifyOtpExpirationMinutes));
        verifyOtp.setRole(Role.CUSTOMER);
        verifyOtp.setFullName(request.getFullName());
        verifyOtp.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        verifyOtp.setPhone(request.getPhone());
        emailVerificationRepository.save(verifyOtp);

        emailService.sendVerificationEmail(request.getEmail(), otp);
    }

    @Override
    @Transactional
    public void registerOwner(OwnerRegisterRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("msg.error.email_exists");
        }

        if (accountRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("msg.error.phone_exists");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("msg.error.passwords_mismatch");
        }

        emailVerificationRepository.deleteByEmail(request.getEmail());

        String otp = generateOtp();
        // Redoing the approach:
        Account account = accountRepository.findByEmail(request.getEmail()).orElse(new Account());
        account.setEmail(request.getEmail());
        account.setFullName(request.getFullName());
        account.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        account.setPhone(request.getPhone());
        account.setRole(Role.OWNER);
        account.setIsActive(false); // Inactive until verify
        account.setCreatedAt(LocalDateTime.now());
        account = accountRepository.save(account);

        OwnerProfile profile = ownerProfileRepository.findByAccountId(account.getId()).orElse(new OwnerProfile());
        profile.setAccount(account);
        profile.setBusinessName(request.getBusinessName());
        profile.setTaxCode(request.getTaxCode());
        profile.setBankName(request.getBankName());
        profile.setBankAccountNo(request.getBankAccountNo());
        profile.setBankAccountName(request.getBankAccountName());
        profile.setApprovalStatus(ApprovalStatus.PENDING);
        ownerProfileRepository.save(profile);

        // Save OTP
        EmailVerification verifyOtp = new EmailVerification();
        verifyOtp.setEmail(request.getEmail());
        verifyOtp.setToken(otp);
        verifyOtp.setExpireAt(LocalDateTime.now().plusMinutes(verifyOtpExpirationMinutes));
        verifyOtp.setRole(Role.OWNER); // indicates this is owner
        verifyOtp.setFullName(request.getFullName());
        verifyOtp.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        verifyOtp.setPhone(request.getPhone());
        emailVerificationRepository.save(verifyOtp);

        emailService.sendVerificationEmail(request.getEmail(), otp);
    }

    @Override
    @Transactional
    public void verifyEmailOtp(VerifyOtpRequest request) {
        Optional<EmailVerification> verificationOpt = emailVerificationRepository.findByTokenAndEmail(request.getOtp(),
                request.getEmail());
        if (verificationOpt.isEmpty()) {
            throw new InvalidOtpException("msg.error.invalid_otp");
        }

        EmailVerification verification = verificationOpt.get();
        if (verification.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new OtpExpiredException("msg.error.expired_otp");
        }

        // Activate account
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("msg.error.account_not_found"));
        account.setIsActive(true);
        accountRepository.save(account);

        emailVerificationRepository.deleteByEmail(request.getEmail());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("msg.error.invalid_credentials"));

        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            throw new RuntimeException("msg.error.invalid_credentials");
        }

        if (account.getIsActive() != null && !account.getIsActive()) {
            throw new RuntimeException("msg.error.account_inactive");
        }

        if (account.getRole() == Role.OWNER) {
            OwnerProfile profile = ownerProfileRepository.findByAccountId(account.getId()).orElse(null);
            if (profile != null && profile.getApprovalStatus() == ApprovalStatus.PENDING) {
                throw new RuntimeException("msg.error.account_pending");
            }
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);

        return AuthResponse.builder()
                .token(token)
                .email(account.getEmail())
                .fullName(account.getFullName())
                .role(account.getRole().name())
                .build();
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("msg.error.account_not_found"));
        
        passwordResetTokenRepository.deleteByEmail(email);

            String otp = generateOtp();
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setEmail(email);
            resetToken.setToken(otp); // Store OTP
            resetToken.setExpireAt(LocalDateTime.now().plusMinutes(resetOtpExpirationMinutes));
            passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(email, otp);
    }

    @Override
    public void verifyResetOtp(VerifyResetOtpRequest request) {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByTokenAndEmail(request.getOtp(),
                request.getEmail());
        if (tokenOpt.isEmpty()) {
            throw new InvalidOtpException("msg.error.invalid_otp");
        }

        PasswordResetToken resetToken = tokenOpt.get();
        if (resetToken.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new OtpExpiredException("msg.error.expired_otp");
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("msg.error.passwords_mismatch");
        }

        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByTokenAndEmail(request.getOtp(),
                request.getEmail());
        if (tokenOpt.isEmpty()) {
            throw new InvalidOtpException("msg.error.invalid_otp");
        }

        PasswordResetToken resetToken = tokenOpt.get();
        if (resetToken.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new OtpExpiredException("msg.error.expired_otp");
        }

        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("msg.error.account_not_found"));

        account.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);

        passwordResetTokenRepository.deleteByEmail(request.getEmail());
    }

    @Override
    @Transactional
    public void completeProfile(String email, CompleteProfileRequest request) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("msg.error.account_not_found"));

        if (!account.getPhone().equals(request.getPhone()) && accountRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("msg.error.phone_exists");
        }

        account.setPhone(request.getPhone());
        accountRepository.save(account);
    }
}
