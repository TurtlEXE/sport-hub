package com.mvc.mock_project.service.impl;

import com.mvc.mock_project.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

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
        
        mailSender.send(message);
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

        mailSender.send(message);
    }
}
