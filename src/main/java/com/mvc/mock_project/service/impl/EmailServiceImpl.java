package com.mvc.mock_project.service.impl;

import com.mvc.mock_project.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;

import jakarta.mail.internet.MimeMessage;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
@Slf4j
@Async
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

    @Override
    public void sendPaymentSuccessEmail(String to, String bookingDetails) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Xác nhận thanh toán tiền sân thành công - SportHub");
        message.setText("Chào bạn,\n\n"
                + "Bạn đã thanh toán tiền sân thành công tại hệ thống SportHub.\n"
                + "Chi tiết đơn hàng:\n"
                + bookingDetails + "\n\n"
                + "LƯU Ý: Số tiền dịch vụ đi kèm (nếu có) sẽ được thanh toán trực tiếp tại sân.\n\n"
                + "Trân trọng,\nĐội ngũ SportHub");

        try {
            mailSender.send(message);
            log.info("Payment success email sent to {}", to);
        } catch (MailException e) {
            log.error("Failed to send payment email to {}", to, e);
            System.out.println("=========================================================");
            System.out.println("DEVELOPMENT MODE - MOCK EMAIL (PAYMENT SUCCESS)");
            System.out.println("To: " + to);
            System.out.println("Booking details: " + bookingDetails);
            System.out.println("=========================================================");
        }
    }

    @Override
    public void sendFeedbackConfirmationEmail(String name, String email, String messageContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("We've Received Your Feedback, " + name + "!");

            String htmlContent = "<div style=\"font-family: sans-serif; padding: 20px; line-height: 1.6; color: #333; max-width: 600px; border: 1px solid #eee; border-radius: 8px;\">"
                    + "<h2 style=\"color: #0066cc;\">Hello " + name + ",</h2>"
                    + "<p>Thank you for reaching out to us. We have received your feedback form submission and wanted to send a copy for your personal records.</p>"
                    + "<div style=\"background-color: #f9f9f9; padding: 15px; border-left: 4px solid #0066cc; margin: 20px 0;\">"
                    + "<h4 style=\"margin-top: 0;\">Your Submitted Details:</h4>"
                    + "<p><strong>Name:</strong> " + name + "</p>"
                    + "<p><strong>Email:</strong> " + email + "</p>"
                    + "<p style=\"margin-bottom: 0;\"><strong>Message:</strong><br>"
                    + messageContent.replace("\n", "<br>") + "</p>"
                    + "</div>"
                    + "<p>Our support team will review your message and reach back out to you if any follow-up actions are required.</p>"
                    + "<hr style=\"border: 0; border-top: 1px solid #eee; margin: 20px 0;\">"
                    + "<p style=\"font-size: 12px; color: #777;\">This is an automated operational notification message. Please do not reply directly to this mail box.</p>"
                    + "</div>";

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Feedback confirmation email sent to {}", email);
        } catch (MessagingException e) {
            log.error("Failed to construct MimeMessage for feedback confirmation to {}", email, e);
        } catch (MailException e) {
            log.error("Failed to send feedback confirmation email to {}. SMTP might not be configured.", email, e);
            System.out.println("=========================================================");
            System.out.println("DEVELOPMENT MODE - MOCK HTML EMAIL");
            System.out.println("To: " + email);
            System.out.println("Subject: We've Received Your Feedback, " + name + "!");
            System.out.println("=========================================================");
        }
    }
}
