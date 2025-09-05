package com.dev.hotelbooking.service.impl;

import com.dev.hotelbooking.dto.response.BookingResponse;
import com.dev.hotelbooking.dto.response.RoomResponse;
import com.dev.hotelbooking.exception.AppException;
import com.dev.hotelbooking.enums.ErrorCode;
import com.dev.hotelbooking.model.BookedRoom;
import com.dev.hotelbooking.model.Room;
import com.dev.hotelbooking.repository.BookingRepository;
import com.dev.hotelbooking.repository.RoomRepository;
import com.dev.hotelbooking.service.IBookingService;
import com.dev.hotelbooking.service.IEmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingService implements IBookingService {
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final IEmailService emailService;

    @Override
    public List<BookedRoom> getAllBookingsByRoomId(Long roomId) {
        return bookingRepository.findAllByRoomId(roomId);
    }

    @Override
    public Page<BookingResponse> getAllBookings(Pageable pageable) {
        Page<BookedRoom> bookings = bookingRepository.findAll(pageable);
        return bookings.map(this::getBookingResponse);
    }

    private BookingResponse getBookingResponse(BookedRoom booking) {
        Room room = roomRepository.findById(booking.getRoom().getId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        RoomResponse roomResponse = new RoomResponse(
                room.getId(),
                room.getRoomType(),
                room.getRoomPrice());
        return new BookingResponse(
                booking.getBookingId(), booking.getCheckInDate(),
                booking.getCheckOutDate(), booking.getGuestFullName(),
                booking.getGuestEmail(), booking.getNumOfAdults(),
                booking.getNumOfChildren(), booking.getTotalNumOfGuest(),
                booking.getBookingConfirmationCode(), roomResponse);
    }

    @Override
    public String saveBooking(Long roomId, BookedRoom bookingRequest) {
        if (bookingRequest.getCheckOutDate().isBefore(bookingRequest.getCheckInDate())) {
            throw new AppException(ErrorCode.INVALID_BOOKING_REQUEST,
                    "Check-in date must come before check-out date");
        }
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        List<BookedRoom> existingBookings = room.getBookings();
        boolean roomIsAvailable = roomIsAvailable(bookingRequest, existingBookings);
        if (!roomIsAvailable) {
            throw new AppException(ErrorCode.INVALID_BOOKING_REQUEST,
                    "Sorry, This room is not available for the selected dates.");
        }
        room.addBooking(bookingRequest);
        BookedRoom savedBooking = bookingRepository.save(bookingRequest);
        emailService.sendBookingConfirmationEmail(savedBooking.getGuestEmail(), savedBooking);
        return bookingRequest.getBookingConfirmationCode();
    }

    @Override
    public BookingResponse findByBookingConfirmationCode(String bookingConfirmationCode) {
        Optional<BookedRoom> optionalBookedRoom =
                bookingRepository.findByBookingConfirmationCode(bookingConfirmationCode);

        String message = "No booking found with booking confirmation code " + bookingConfirmationCode;

        if (optionalBookedRoom.isEmpty() || optionalBookedRoom.get().getCheckOutDate().isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND, message);
        }

        BookedRoom bookedRoom = optionalBookedRoom.get();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        if (!bookedRoom.getGuestEmail().equalsIgnoreCase(email)) {
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND, message);
        }

        return getBookingResponse(bookedRoom);
    }

    @Override
    public List<BookingResponse> getBookingsByUserEmail(String email) {
        List<BookedRoom> bookings = bookingRepository.findAllByGuestEmail(email);
        List<BookingResponse> bookingResponses = new ArrayList<>();
        for (BookedRoom booking : bookings) {
            BookingResponse bookingResponse = getBookingResponse(booking);
            bookingResponses.add(bookingResponse);
        }

        return bookingResponses;
    }

    @Override
    public void cancelBooking(Long bookingId) {
        BookedRoom bookedRoom = getBookingById(bookingId);
        List<BookedRoom> existingBookings = bookingRepository.findAllByRoomId(bookedRoom.getRoom().getId());
        if (existingBookings.size() == 1) {
            bookedRoom.getRoom().setBooked(false);
        }
        bookingRepository.deleteById(bookingId);
    }

    @Override
    public void cancelAllBookings() {
        List<Room> rooms = roomRepository.findAllByIsBooked(true);
        rooms.forEach(room -> room.setBooked(false));
        bookingRepository.deleteAll();
    }

    private boolean roomIsAvailable(BookedRoom bookingRequest, List<BookedRoom> existingBookings) {
        return existingBookings.stream().noneMatch(existingBooking ->
                !(bookingRequest.getCheckOutDate().isBefore(existingBooking.getCheckInDate())
                        || bookingRequest.getCheckInDate().isAfter(existingBooking.getCheckOutDate()))
        );
    }

    @Override
    public BookedRoom getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND,
                        "No booking found with booking id " + bookingId));
    }
}
