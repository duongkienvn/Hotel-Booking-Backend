package com.dev.hotelbooking.repository;

import com.dev.hotelbooking.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {
    @Query("SELECT DISTINCT r.roomType FROM Room r")
    List<String> findAllRoomTypes();

    @Query("SELECT r FROM Room r " +
            "WHERE LOWER(r.roomType) LIKE LOWER(CONCAT('%', :roomType, '%')) " +
            "AND r.id NOT IN (" +
            "  SELECT br.room.id FROM BookedRoom br " +
            "  WHERE (br.checkInDate <= :checkOutDate AND br.checkOutDate >= :checkInDate)" +
            ")")
    List<Room> findAvailableRoomsByDatesAndType(LocalDate checkInDate, LocalDate checkOutDate, String roomType);
    List<Room> findAllByIsBooked(boolean isBooked);
}
