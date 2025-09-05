package com.dev.hotelbooking.repository;

import com.dev.hotelbooking.model.BookedRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<BookedRoom, Long> {
    List<BookedRoom> findAllByRoomId(Long roomId);
    Optional<BookedRoom> findByBookingConfirmationCode(String bookingConfirmationCode);
    List<BookedRoom> findAllByGuestEmail(String email);
}
