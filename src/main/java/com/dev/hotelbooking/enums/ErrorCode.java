package com.dev.hotelbooking.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION("Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    ROOM_NOT_FOUND("Room not found", HttpStatus.NOT_FOUND),
    PHOTO_RETRIEVAL("Error retrieving photo", HttpStatus.NOT_FOUND),
    UPDATE_FAIL("Fail updating room", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_BOOKING_REQUEST("Invalid booking request", HttpStatus.BAD_REQUEST),
    BOOKING_NOT_FOUND("Booking not found", HttpStatus.NOT_FOUND),
    VALIDATION_ERROR("Validation failed!", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED("User not existed", HttpStatus.NOT_FOUND),
    EMAIL_EXISTED("Email is already existed", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED("Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("You do not have permission", HttpStatus.FORBIDDEN),
    BAD_CREDENTIALS("Phone number or password is wrong!", HttpStatus.BAD_REQUEST),
    ROLE_EXISTED("Role is already existed", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND("Role not found", HttpStatus.BAD_REQUEST),
    EMAIL_SEND_FAILED("Failed to send email to user", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_TOKEN("Invalid token", HttpStatus.BAD_REQUEST),
    INVALID_USERDETAILS("Invalid user details", HttpStatus.BAD_REQUEST),
    FAILED_TO_SAVE_IMAGE("Failed to save image", HttpStatus.INTERNAL_SERVER_ERROR),
    TESTIMONIAL_NOT_FOUND("Testimonial not found", HttpStatus.NOT_FOUND),
    ;

    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String message, HttpStatus httpStatus) {
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
