package com.dev.hotelbooking.service.impl;

import com.dev.hotelbooking.dto.response.BookingResponse;
import com.dev.hotelbooking.dto.response.RoomResponse;
import com.dev.hotelbooking.enums.ErrorCode;
import com.dev.hotelbooking.exception.AppException;
import com.dev.hotelbooking.model.BookedRoom;
import com.dev.hotelbooking.model.Room;
import com.dev.hotelbooking.repository.BookingRepository;
import com.dev.hotelbooking.repository.RoomRepository;
import com.dev.hotelbooking.service.IRoomService;
import com.dev.hotelbooking.specification.RoomSpecs;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomService implements IRoomService {
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    @Override
    public Room addNewRoom(MultipartFile file, String roomType, BigDecimal roomPrice) throws IOException, SQLException {
        Room room = new Room();
        room.setRoomType(roomType);
        room.setRoomPrice(roomPrice);
        if (!file.isEmpty()) {
            byte[] photoBytes = file.getBytes();
            room.setPhoto(photoBytes);
        }
        Room newRoom = roomRepository.save(room);

        return newRoom;
    }

    @Override
    public List<String> getAllRoomTypes() {
        return roomRepository.findAllRoomTypes();
    }

    @Override
    public Page<RoomResponse> getAllRooms(Pageable pageable) throws SQLException {
        Page<Room> rooms = roomRepository.findAll(pageable);

        return rooms.map(room -> {
            byte[] photoBytes = getRoomPhotoByRoomId(room.getId());

            RoomResponse roomResponse = getRoomResponse(room);

            if (photoBytes != null && photoBytes.length > 0) {
                String base64Photo = Base64.getEncoder().encodeToString(photoBytes);
                roomResponse.setPhoto(base64Photo);
            }

            return roomResponse;
        });
    }

    private byte[] getRoomPhotoByRoomId(Long roomId) {
        Room room = getRoomById(roomId);
        byte[] photoBytes = room.getPhoto();
        if (photoBytes != null) {
            return photoBytes;
        }
        return new byte[0];
    }

    private RoomResponse getRoomResponse(Room room) {
        List<BookedRoom> bookings = bookingRepository.findAllByRoomId(room.getId());
        List<BookingResponse> bookingInfo = bookings
                .stream()
                .map(booking -> new BookingResponse(booking.getBookingId(),
                        booking.getCheckInDate(),
                        booking.getCheckOutDate(), booking.getBookingConfirmationCode())).toList();
        byte[] photoBytes = room.getPhoto();
        if (photoBytes == null) {
            throw new AppException(ErrorCode.PHOTO_RETRIEVAL);
        }
        return new RoomResponse(room.getId(),
                room.getRoomType(), room.getRoomPrice(),
                room.isBooked(), photoBytes, bookingInfo);
    }

    @Override
    public void deleteRoomById(Long roomId) {
        getRoomById(roomId);
        roomRepository.deleteById(roomId);
    }

    @Override
    public RoomResponse updateRoom(Long roomId, String roomType, BigDecimal roomPrice, MultipartFile photo)
            throws IOException, SQLException {
        Room room = getRoomById(roomId);
        if (roomType != null) room.setRoomType(roomType);
        if (roomPrice != null) room.setRoomPrice(roomPrice);
        byte[] photoBytes = photo != null && !photo.isEmpty() ? photo.getBytes() : getRoomPhotoByRoomId(roomId);
        room.setPhoto(photoBytes);
        Room savedRoom = roomRepository.save(room);
        return getRoomResponse(savedRoom);
    }

    @Override
    public Room getRoomById(Long roomId) {
        Optional<Room> room = roomRepository.findById(roomId);
        if (room.isEmpty()) {
            throw new AppException(ErrorCode.ROOM_NOT_FOUND);
        }
        return room.get();
    }

    @Override
    public RoomResponse getRoomByRoomId(Long roomId) {
        Room room = getRoomById(roomId);
        return getRoomResponse(room);
    }

    @Override
    public List<RoomResponse> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate, String roomType) throws SQLException {
        List<Room> availableRooms = roomRepository.findAvailableRoomsByDatesAndType(checkInDate, checkOutDate, roomType);
        List<RoomResponse> roomResponses = new ArrayList<>();
        for (Room room : availableRooms) {
            byte[] photoBytes = getRoomPhotoByRoomId(room.getId());
            String photoBase64 = Base64.getEncoder().encodeToString(photoBytes);
            RoomResponse roomResponse = getRoomResponse(room);
            roomResponse.setPhoto(photoBase64);
            roomResponses.add(roomResponse);
        }

        return roomResponses;
    }

    @Override
    public Page<RoomResponse> filterRoomsByCriteria(Map<String, String> criteria, Pageable pageable) {
        Specification<Room> specification = Specification.allOf();

        String roomType = criteria.get("roomType");
        String minPriceStr = criteria.get("minPrice");
        String maxPriceStr = criteria.get("maxPrice");
        String checkInDateStr = criteria.get("checkInDate");
        String checkOutDateStr = criteria.get("checkOutDate");

        BigDecimal minPrice = (minPriceStr != null && !minPriceStr.isEmpty())
                ? new BigDecimal(minPriceStr)
                : null;

        BigDecimal maxPrice = (maxPriceStr != null && !maxPriceStr.isEmpty())
                ? new BigDecimal(maxPriceStr)
                : null;

        LocalDate checkInDate = (checkInDateStr != null && !checkInDateStr.isEmpty())
                ? LocalDate.parse(checkInDateStr)
                : null;

        LocalDate checkOutDate = (checkOutDateStr != null && !checkOutDateStr.isEmpty())
                ? LocalDate.parse(checkOutDateStr)
                : null;

        if (roomType != null && !roomType.isBlank()) {
            specification = specification.and(RoomSpecs.fieldContains("roomType", roomType.trim()));
        }

        if (minPrice != null) {
            specification = specification.and(RoomSpecs.priceGreaterThan("roomPrice", minPrice));
        }

        if (maxPrice != null) {
            specification = specification.and(RoomSpecs.priceLessThan("roomPrice", maxPrice));
        }

        if (checkInDate != null && checkOutDate != null) {
            specification = specification.and(RoomSpecs.availableBetween(checkInDate, checkOutDate));
        }

        Page<Room> rooms = roomRepository.findAll(specification, pageable);
        return rooms.map(this::getRoomResponse);
    }

    @Override
    public List<RoomResponse> getAvailableRooms() {
        List<Room> rooms = roomRepository.findAllByIsBooked(false);
        if (rooms.size() >= 8) {
            return rooms.stream().map(this::getRoomResponse).toList().subList(0, 8);
        }
        return roomRepository.findAll()
                .stream()
                .map(this::getRoomResponse)
                .toList().subList(0, 8);
    }
}
