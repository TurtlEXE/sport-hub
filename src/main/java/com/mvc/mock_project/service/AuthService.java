package com.mvc.mock_project.service;

import com.mvc.mock_project.dto.request.*;
import com.mvc.mock_project.dto.response.AuthResponse;

public interface AuthService {
    void registerCustomer(RegisterRequest request);
    void registerOwner(OwnerRegisterRequest request);
    void verifyEmailOtp(VerifyOtpRequest request);
    AuthResponse login(LoginRequest request);
    void forgotPassword(String email);
    void verifyResetOtp(VerifyResetOtpRequest request);
    void resetPassword(ResetPasswordRequest request);
    void completeProfile(String email, CompleteProfileRequest request);
}
