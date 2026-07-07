package com.mvc.mock_project.service;

public interface EmailService {
    void sendVerificationEmail(String to, String otp);
    void sendPasswordResetEmail(String to, String otp);
    void sendContactEmail(String name, String email, String message);
}
