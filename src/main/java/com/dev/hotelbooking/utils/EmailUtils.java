package com.dev.hotelbooking.utils;

import com.dev.hotelbooking.model.BookedRoom;

public class EmailUtils {
    private EmailUtils() {
        throw new UnsupportedOperationException("Utility class!");
    }

    public static String getEmailBookingConfirmation(BookedRoom booking) {
        return "Dear " + booking.getGuestFullName() + ",\n\n" +
                "Thank you for choosing our hotel! Your booking has been successfully confirmed.\n\n" +
                "Booking Details:\n" +
                "---------------------------------------------\n" +
                "Confirmation Code: " + booking.getBookingConfirmationCode() + "\n" +
                "Guest Name: " + booking.getGuestFullName() + "\n" +
                "Check-in Date: " + booking.getCheckInDate() + "\n" +
                "Check-out Date: " + booking.getCheckOutDate() + "\n" +
                "Room Type: " + (booking.getRoom() != null ? booking.getRoom().getRoomType() : "N/A") + "\n" +
                "Guests: " + booking.getNumOfAdults() + " Adults, " + booking.getNumOfChildren() + " Children\n" +
                "Total Guests: " + booking.getTotalNumOfGuest() + "\n" +
                "---------------------------------------------\n\n" +
                "Please keep this confirmation code safe, as you will need it during check-in.\n\n" +
                "If you have any questions or need assistance, feel free to reply to this email or contact our support team.\n\n" +
                "We look forward to welcoming you soon!\n\n" +
                "Best regards,\n" +
                "Hotel Booking Team";
    }

    public static String getEmailMessage(String name, String host, String token) {
        return "Hello " + name + ",\n\nYour new account has been created. Please click on the link below to verify " +
                "your account.\n\n" + getVerification(host, token) + "\n\nThe Support Team";
    }

    public static String getVerification(String host, String token) {
        return host + "/users/verify/account?token=" + token;
    }

    public static String getResetPasswordMessage(String name, String host, String token) {
        return "Hello " + name + ",\n\n" +
                "We received a request to reset your password. Please click the link below to set a new password:\n\n" +
                getResetPasswordUrl(host, token) + "\n\n" +
                "If you did not request a password reset, please ignore this email.\n\n" +
                "Best regards,\n" +
                "The Support Team";
    }

    public static String getResetPasswordUrl(String host, String token) {
        return host + "/users/verify/password?token=" + token;
    }
}
