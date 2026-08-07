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
    public void sendPaymentSuccessEmail(String to, String userName, String transactionId, String time, String facilityName, java.util.List<String> courtDetails) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Booking Confirmation - SportHub");

            StringBuilder courtDetailsHtml = new StringBuilder();
            if (courtDetails != null) {
                for (String detail : courtDetails) {
                    courtDetailsHtml.append("<li>").append(detail).append("</li>");
                }
            }

            String htmlContent = "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"UTF-8\">\n" +
                    "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                    "    <title>Booking Confirmation - SportHub</title>\n" +
                    "    <style>\n" +
                    "        body {\n" +
                    "            font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;\n" +
                    "            background-color: #f4f7f6;\n" +
                    "            margin: 0;\n" +
                    "            padding: 0;\n" +
                    "            color: #333333;\n" +
                    "        }\n" +
                    "        .email-wrapper {\n" +
                    "            width: 100%;\n" +
                    "            background-color: #f4f7f6;\n" +
                    "            padding: 40px 15px;\n" +
                    "            box-sizing: border-box;\n" +
                    "        }\n" +
                    "        .email-content {\n" +
                    "            max-width: 600px;\n" +
                    "            margin: 0 auto;\n" +
                    "            background-color: #ffffff;\n" +
                    "            border-radius: 8px;\n" +
                    "            overflow: hidden;\n" +
                    "            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);\n" +
                    "        }\n" +
                    "        .header {\n" +
                    "            background-color: #10b981;\n" +
                    "            padding: 30px 20px;\n" +
                    "            text-align: center;\n" +
                    "        }\n" +
                    "        .header h1 {\n" +
                    "            color: #ffffff;\n" +
                    "            margin: 0;\n" +
                    "            font-size: 26px;\n" +
                    "            font-weight: 700;\n" +
                    "            letter-spacing: 1px;\n" +
                    "        }\n" +
                    "        .body-content {\n" +
                    "            padding: 35px 40px;\n" +
                    "        }\n" +
                    "        .body-content p {\n" +
                    "            font-size: 16px;\n" +
                    "            line-height: 1.6;\n" +
                    "            margin-top: 0;\n" +
                    "            margin-bottom: 20px;\n" +
                    "            color: #4b5563;\n" +
                    "        }\n" +
                    "        .greeting {\n" +
                    "            font-size: 18px !important;\n" +
                    "            font-weight: 600;\n" +
                    "            color: #1f2937 !important;\n" +
                    "        }\n" +
                    "        .order-details {\n" +
                    "            background-color: #f9fafb;\n" +
                    "            border: 1px solid #e5e7eb;\n" +
                    "            border-radius: 8px;\n" +
                    "            padding: 25px;\n" +
                    "            margin-bottom: 25px;\n" +
                    "        }\n" +
                    "        .order-details h2 {\n" +
                    "            font-size: 18px;\n" +
                    "            margin-top: 0;\n" +
                    "            margin-bottom: 20px;\n" +
                    "            color: #1f2937;\n" +
                    "            border-bottom: 2px solid #e5e7eb;\n" +
                    "            padding-bottom: 10px;\n" +
                    "        }\n" +
                    "        .detail-row {\n" +
                    "            margin-bottom: 12px;\n" +
                    "        }\n" +
                    "        .detail-label {\n" +
                    "            font-weight: 600;\n" +
                    "            color: #6b7280;\n" +
                    "            display: block;\n" +
                    "            font-size: 13px;\n" +
                    "            text-transform: uppercase;\n" +
                    "            letter-spacing: 0.5px;\n" +
                    "            margin-bottom: 4px;\n" +
                    "        }\n" +
                    "        .detail-value {\n" +
                    "            color: #111827;\n" +
                    "            font-size: 16px;\n" +
                    "            font-weight: 500;\n" +
                    "        }\n" +
                    "        .detail-value ul {\n" +
                    "            margin: 5px 0 0 0;\n" +
                    "            padding-left: 20px;\n" +
                    "            font-weight: normal;\n" +
                    "        }\n" +
                    "        .detail-value ul li {\n" +
                    "            margin-bottom: 5px;\n" +
                    "        }\n" +
                    "        .note {\n" +
                    "            background-color: #fffbeb;\n" +
                    "            border-left: 4px solid #f59e0b;\n" +
                    "            padding: 16px 20px;\n" +
                    "            border-radius: 4px;\n" +
                    "            font-size: 14px;\n" +
                    "            color: #92400e;\n" +
                    "            margin-bottom: 30px;\n" +
                    "            line-height: 1.5;\n" +
                    "        }\n" +
                    "        .note strong {\n" +
                    "            color: #b45309;\n" +
                    "        }\n" +
                    "        .footer {\n" +
                    "            background-color: #f3f4f6;\n" +
                    "            padding: 25px;\n" +
                    "            text-align: center;\n" +
                    "            font-size: 13px;\n" +
                    "            color: #6b7280;\n" +
                    "            border-top: 1px solid #e5e7eb;\n" +
                    "        }\n" +
                    "        .footer p {\n" +
                    "            margin: 0;\n" +
                    "            line-height: 1.5;\n" +
                    "        }\n" +
                    "    </style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "    <div class=\"email-wrapper\">\n" +
                    "        <div class=\"email-content\">\n" +
                    "            <div class=\"header\">\n" +
                    "                <h1>SportHub</h1>\n" +
                    "            </div>\n" +
                    "            \n" +
                    "            <div class=\"body-content\">\n" +
                    "                <p class=\"greeting\">Hello " + userName + ",</p>\n" +
                    "                \n" +
                    "                <p>Your payment for the court booking has been processed successfully at SportHub.</p>\n" +
                    "                \n" +
                    "                <div class=\"order-details\">\n" +
                    "                    <h2>Booking Details</h2>\n" +
                    "                    \n" +
                    "                    <div class=\"detail-row\">\n" +
                    "                        <span class=\"detail-label\">Transaction ID</span>\n" +
                    "                        <span class=\"detail-value\">" + transactionId + "</span>\n" +
                    "                    </div>\n" +
                    "                    \n" +
                    "                    <div class=\"detail-row\">\n" +
                    "                        <span class=\"detail-label\">Time</span>\n" +
                    "                        <span class=\"detail-value\">" + time + "</span>\n" +
                    "                    </div>\n" +
                    "                    \n" +
                    "                    <div class=\"detail-row\">\n" +
                    "                        <span class=\"detail-label\">Facility</span>\n" +
                    "                        <span class=\"detail-value\">" + facilityName + "</span>\n" +
                    "                    </div>\n" +
                    "                    \n" +
                    "                    <div class=\"detail-row\">\n" +
                    "                        <span class=\"detail-label\">Court Details</span>\n" +
                    "                        <div class=\"detail-value\">\n" +
                    "                            <ul>\n" +
                    "                                " + courtDetailsHtml.toString() + "\n" +
                    "                            </ul>\n" +
                    "                        </div>\n" +
                    "                    </div>\n" +
                    "                </div>\n" +
                    "                \n" +
                    "                <div class=\"note\">\n" +
                    "                    <strong>NOTE:</strong> Payment for any additional services (if applicable) will be made directly at the facility.\n" +
                    "                </div>\n" +
                    "                \n" +
                    "                <p>Best regards,<br><strong>The SportHub Team</strong></p>\n" +
                    "            </div>\n" +
                    "            \n" +
                    "            <div class=\"footer\">\n" +
                    "                <p>&copy; 2026 SportHub. All rights reserved.</p>\n" +
                    "                <p>If you have any questions, please reply directly to this email.</p>\n" +
                    "            </div>\n" +
                    "        </div>\n" +
                    "    </div>\n" +
                    "</body>\n" +
                    "</html>";

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Payment success email sent to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to construct MimeMessage for payment success to {}", to, e);
        } catch (MailException e) {
            log.error("Failed to send payment email to {}", to, e);
            System.out.println("=========================================================");
            System.out.println("DEVELOPMENT MODE - MOCK EMAIL (PAYMENT SUCCESS)");
            System.out.println("To: " + to);
            System.out.println("Transaction ID: " + transactionId);
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
