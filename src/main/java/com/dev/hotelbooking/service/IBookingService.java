package com.dev.hotelbooking.service;

import com.dev.hotelbooking.dto.response.BookingResponse;
import com.dev.hotelbooking.model.BookedRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IBookingService {
    List<BookedRoom> getAllBookingsByRoomId(Long roomId);
    Page<BookingResponse> getAllBookings(Pageable pageable);
    String saveBooking(Long roomId, BookedRoom bookingRequest);
    BookingResponse findByBookingConfirmationCode(String bookingConfirmationCode);
    List<BookingResponse> getBookingsByUserEmail(String email);
    void cancelBooking(Long bookingId);
    void cancelAllBookings();
    BookedRoom getBookingById(Long bookingId);
}
