package com.dev.hotelbooking.controller;

import com.dev.hotelbooking.dto.response.BookingResponse;
import com.dev.hotelbooking.dto.response.PageResponse;
import com.dev.hotelbooking.model.BookedRoom;
import com.dev.hotelbooking.service.IBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final IBookingService bookingService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<Object>> getAllBookings(@PageableDefault(size = 10, page = 0)
                                                                   Pageable pageable) {
        Page<BookingResponse> bookingResponsePage = bookingService.getAllBookings(pageable);
        List<BookingResponse> bookingResponses = bookingResponsePage.getContent();
        int totalPages = bookingResponsePage.getTotalPages();
        long totalElements = bookingResponsePage.getTotalElements();

        return ResponseEntity.ok(PageResponse.builder()
                .totalElements(totalElements)
                .totalPages(totalPages)
                .content(bookingResponses)
                .build());
    }

    @PostMapping("/rooms/{roomId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> saveBooking(@PathVariable Long roomId,
                                              @RequestBody BookedRoom bookingRequest) {
        String confirmationCode = bookingService.saveBooking(roomId, bookingRequest);
        return ResponseEntity.ok(
                "Room booked successfully, Your booking confirmation code is: " + confirmationCode);
    }

    @GetMapping("/confirmation/{confirmationCode}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<BookingResponse> getBookingByConfirmationCode(@PathVariable String confirmationCode) {
        BookingResponse bookingResponse = bookingService.findByBookingConfirmationCode(confirmationCode);
        return ResponseEntity.ok(bookingResponse);
    }

    @GetMapping("/users/{email}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #email == authentication.name)")
    public ResponseEntity<List<BookingResponse>> getBookingsByUserEmail(@PathVariable String email) {
        List<BookingResponse> bookingResponses = bookingService.getBookingsByUserEmail(email);
        return ResponseEntity.ok(bookingResponses);
    }

    @DeleteMapping("/{bookingId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and @bookingService.getBookingById(#bookingId).guestEmail" +
            " == authentication.name)")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancelAllBookings() {
        bookingService.cancelAllBookings();
        return ResponseEntity.noContent().build();
    }
}
