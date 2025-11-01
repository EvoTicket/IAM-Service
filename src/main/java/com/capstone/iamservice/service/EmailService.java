package com.capstone.iamservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendForgotPasswordOtp(String to, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Mã OTP đặt lại mật khẩu");
        message.setText(
                "Xin chào,\n\n" +
                        "Bạn đã yêu cầu đặt lại mật khẩu.\n" +
                        "Mã OTP của bạn là: " + otpCode + "\n" +
                        "Mã này sẽ hết hạn sau 5 phút.\n\n" +
                        "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n" +
                        "Trân trọng!"
        );
        mailSender.send(message);
    }

    public void sendEmailVerificationOtp(String to, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Mã OTP xác thực email");
        message.setText(
                "Xin chào,\n\n" +
                        "Cảm ơn bạn đã đăng ký tài khoản.\n" +
                        "Mã OTP xác thực email của bạn là: " + otpCode + "\n" +
                        "Mã này sẽ hết hạn sau 5 phút.\n\n" +
                        "Vui lòng nhập mã này để hoàn tất việc xác thực email.\n\n" +
                        "Trân trọng!"
        );
        mailSender.send(message);
    }

    public void sendChangePasswordOtp(String to, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Mã OTP xác nhận đổi mật khẩu");
        message.setText(
                "Xin chào,\n\n" +
                        "Bạn đã yêu cầu đổi mật khẩu.\n" +
                        "Mã OTP xác nhận của bạn là: " + otpCode + "\n" +
                        "Mã này sẽ hết hạn sau 5 phút.\n\n" +
                        "Nếu bạn không yêu cầu đổi mật khẩu, vui lòng bỏ qua email này.\n\n" +
                        "Trân trọng!"
        );
        mailSender.send(message);
    }
}
