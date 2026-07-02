package com.mvc.mock_project.controller;

import com.mvc.mock_project.dto.request.*;
import com.mvc.mock_project.dto.response.ApiResponse;
import com.mvc.mock_project.dto.response.AuthResponse;
import com.mvc.mock_project.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        AuthResponse response = authService.login(request);
        
        SecurityContext context = SecurityContextHolder.getContext();
        SecurityContextRepository repository = new HttpSessionSecurityContextRepository();
        repository.saveContext(context, httpRequest, httpResponse);
        
        return ResponseEntity.ok(ApiResponse.success("msg.success.login", response));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registerCustomer(@Valid @RequestBody RegisterRequest request) {
        authService.registerCustomer(request);
        return ResponseEntity.ok(ApiResponse.success("msg.success.register"));
    }

    @PostMapping("/register/owner")
    public ResponseEntity<ApiResponse<Void>> registerOwner(@Valid @RequestBody OwnerRegisterRequest request) {
        authService.registerOwner(request);
        return ResponseEntity.ok(ApiResponse.success("msg.success.register"));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmailOtp(@Valid @RequestBody VerifyOtpRequest request) {
        authService.verifyEmailOtp(request);
        return ResponseEntity.ok(ApiResponse.success("msg.success.verify"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("msg.success.forgot"));
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<ApiResponse<Void>> verifyResetOtp(@Valid @RequestBody VerifyResetOtpRequest request) {
        authService.verifyResetOtp(request);
        return ResponseEntity.ok(ApiResponse.success("msg.success.verify"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("msg.success.reset"));
    }

    @PostMapping("/complete-profile")
    public ResponseEntity<ApiResponse<Void>> completeProfile(@Valid @RequestBody CompleteProfileRequest request,
                                                             Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Unauthorized"));
        }
        
        String email = "";
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.mvc.mock_project.security.CustomOAuth2User) {
            email = ((com.mvc.mock_project.security.CustomOAuth2User) principal).getAccount().getEmail();
        } else if (principal instanceof com.mvc.mock_project.security.CustomUserDetails) {
            email = ((com.mvc.mock_project.security.CustomUserDetails) principal).getUsername();
        } else {
            email = authentication.getName();
        }

        authService.completeProfile(email, request);
        return ResponseEntity.ok(ApiResponse.success("msg.success.profile"));
    }
}
