package com.dev.hotelbooking.specification;

import com.dev.hotelbooking.model.BookedRoom;
import com.dev.hotelbooking.model.Room;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

public class RoomSpecs {
    private RoomSpecs() {
        throw new UnsupportedOperationException("Utility class!");
    }

    public static Specification<Room> fieldEquals(String fieldName, Object value) {
        return (root, query, cb) ->
                cb.equal(root.get(fieldName), value);
    }

    public static Specification<Room> fieldContains(String fieldName, String value) {
        return (root, query, cb) ->
                cb.like(root.get(fieldName), "%" + value.toLowerCase() + "%");
    }

    public static Specification<Room> priceGreaterThan(String fieldName, BigDecimal minPrice) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get(fieldName), minPrice);
    }

    public static Specification<Room> priceLessThan(String fieldName, BigDecimal maxPrice) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get(fieldName), maxPrice);
    }

    public static Specification<Room> availableBetween(LocalDate desiredCheckIn, LocalDate desiredCheckOut) {
        return (root, query, cb) -> {
            Join<Room, BookedRoom> bookings = root.join("bookings", JoinType.LEFT);

            var overlap = cb.and(
                    cb.lessThan(bookings.get("checkInDate"), desiredCheckOut),
                    cb.greaterThan(bookings.get("checkOutDate"), desiredCheckIn)
            );
            return cb.or(cb.isNull(bookings.get("bookingId")), cb.not(overlap));
        };
    }
}
