package com.dev.hotelbooking.service;

import com.dev.hotelbooking.model.BookedRoom;

public interface IEmailService {
    void sendNewAccountEmail(String name, String email, String token);
    void sendBookingConfirmationEmail(String email, BookedRoom bookedRoom);
    void sendResetPasswordEmail(String name, String email, String token);
}
