package com.mvc.mock_project.service;

public interface EmailService {
    void sendVerificationEmail(String to, String otp);
    void sendPasswordResetEmail(String to, String otp);
<<<<<<< HEAD
    void sendPaymentSuccessEmail(String to, String bookingDetails);
=======
    void sendContactEmail(String name, String email, String message);
>>>>>>> 7cad2fe1b37be4638c52b0d426a1a7231c70093e
}
