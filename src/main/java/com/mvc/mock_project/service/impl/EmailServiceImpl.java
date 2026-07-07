package com.mvc.mock_project.service.impl;

import com.mvc.mock_project.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.admin.email:support@sporthub.com}")
    private String adminEmail;

    @Override
    public void sendVerificationEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Xác thực tài khoản SportHub - OTP của bạn");
        message.setText("Chào bạn,\n\n"
                + "Cảm ơn bạn đã đăng ký tài khoản tại SportHub.\n"
                + "Mã OTP 6 số để kích hoạt tài khoản của bạn là: " + otp + "\n\n"
                + "Mã này sẽ hết hạn sau 24 giờ.\n\n"
                + "Trân trọng,\nĐội ngũ SportHub");
        
        try {
            mailSender.send(message);
            log.info("Verification email sent to {}", to);
        } catch (MailException e) {
            log.error("Failed to send email to {}. SMTP might not be configured. OTP is: {}", to, otp, e);
            System.out.println("=========================================================");
            System.out.println("DEVELOPMENT MODE - MOCK EMAIL");
            System.out.println("To: " + to);
            System.out.println("OTP: " + otp);
            System.out.println("=========================================================");
        }
    }

    @Override
    public void sendPasswordResetEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Đặt lại mật khẩu SportHub - OTP của bạn");
        message.setText("Chào bạn,\n\n"
                + "Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.\n"
                + "Mã OTP 6 số để đặt lại mật khẩu của bạn là: " + otp + "\n\n"
                + "Mã này sẽ hết hạn sau 10 phút. Nếu bạn không yêu cầu đặt lại mật khẩu, xin hãy bỏ qua email này.\n\n"
                + "Trân trọng,\nĐội ngũ SportHub");

        try {
            mailSender.send(message);
            log.info("Password reset email sent to {}", to);
        } catch (MailException e) {
            log.error("Failed to send email to {}. SMTP might not be configured. OTP is: {}", to, otp, e);
            System.out.println("=========================================================");
            System.out.println("DEVELOPMENT MODE - MOCK EMAIL");
            System.out.println("To: " + to);
            System.out.println("Reset OTP: " + otp);
        }
    }

    @Override
    public void sendContactEmail(String name, String email, String messageContent) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(adminEmail);
        message.setSubject("Yêu cầu liên hệ mới từ: " + name);
        message.setText("Hệ thống vừa nhận được một tin nhắn liên hệ mới.\n\n"
                + "Thông tin người gửi:\n"
                + "- Tên: " + name + "\n"
                + "- Email: " + email + "\n\n"
                + "Nội dung tin nhắn:\n"
                + messageContent + "\n\n"
                + "Trân trọng,\nHệ thống SportHub");

        try {
            mailSender.send(message);
            log.info("Contact email sent from {}", email);
        } catch (MailException e) {
            log.error("Failed to send contact email from {}. SMTP might not be configured.", email, e);
            System.out.println("=========================================================");
            System.out.println("DEVELOPMENT MODE - MOCK EMAIL");
            System.out.println("To: " + adminEmail);
            System.out.println("From: " + email);
            System.out.println("Message: " + messageContent);
            System.out.println("=========================================================");
        }
    }
}
