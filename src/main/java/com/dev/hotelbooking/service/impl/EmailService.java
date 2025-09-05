package com.dev.hotelbooking.service.impl;

import com.dev.hotelbooking.exception.AppException;
import com.dev.hotelbooking.enums.ErrorCode;
import com.dev.hotelbooking.model.BookedRoom;
import com.dev.hotelbooking.service.IEmailService;
import com.dev.hotelbooking.utils.EmailUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static com.dev.hotelbooking.utils.EmailUtils.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService implements IEmailService {
    public static final String NEW_USER_ACCOUNT_VERIFICATION = "New User Account Verification";
    public static final String PASSWORD_RESET_REQUEST = "Password Reset Request";
    public static final String BOOKING_CONFIRMATION = "Booking Confirmation";
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;
    @Value("${spring.mail.verify.host}")
    private String host;

    @Override
    @Async
    public void sendNewAccountEmail(String name, String email, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject(NEW_USER_ACCOUNT_VERIFICATION);
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setText(getEmailMessage(name, host, token));
            javaMailSender.send(message);
        } catch (Exception e) {
            handleError(email);
        }
    }

    @Override
    @Async
    public void sendBookingConfirmationEmail(String email, BookedRoom bookedRoom) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject(BOOKING_CONFIRMATION);
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setText(getEmailBookingConfirmation(bookedRoom));
            javaMailSender.send(message);
            log.info("Email sent to user: {}", email);
        } catch (Exception e) {
            handleError(email);
        }
    }

    @Override
    @Async
    public void sendResetPasswordEmail(String name, String email, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject(PASSWORD_RESET_REQUEST);
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setText(getResetPasswordMessage(name, host, token));
            javaMailSender.send(message);
        } catch (Exception e) {
            handleError(email);
        }
    }

    private void handleError(String email) {
        log.error("Error sending email to user: {}", email);
        throw new AppException(ErrorCode.EMAIL_SEND_FAILED);
    }
}
