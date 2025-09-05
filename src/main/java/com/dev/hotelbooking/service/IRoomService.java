package com.dev.hotelbooking.service;

import com.dev.hotelbooking.dto.response.RoomResponse;
import com.dev.hotelbooking.model.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface IRoomService {
    Room addNewRoom(MultipartFile file, String roomType, BigDecimal roomPrice) throws IOException, SQLException;
    List<String> getAllRoomTypes();
    Page<RoomResponse> getAllRooms(Pageable pageable) throws SQLException;
    void deleteRoomById(Long roomId);
    RoomResponse updateRoom(Long roomId, String roomType, BigDecimal roomPrice, MultipartFile file) throws IOException, SQLException;
    RoomResponse getRoomByRoomId(Long roomId);
    Room getRoomById(Long roomId);
    List<RoomResponse> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate, String roomType) throws SQLException;
    Page<RoomResponse> filterRoomsByCriteria(Map<String, String> criteria, Pageable pageable);
    List<RoomResponse> getAvailableRooms();
}
