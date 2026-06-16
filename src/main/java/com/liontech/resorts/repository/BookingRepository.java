package com.liontech.resorts.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.liontech.resorts.domain.Booking;
import com.liontech.resorts.domain.BookingStatus;
import com.liontech.resorts.domain.Room;
import com.liontech.resorts.domain.UserAccount;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByReference(String reference);

    List<Booking> findByGuestOrderByCreatedAtDesc(UserAccount guest);

    boolean existsByRoomAndStatusInAndCheckInDateLessThanAndCheckOutDateGreaterThan(
        Room room,
        Collection<BookingStatus> statuses,
        LocalDate checkOutDate,
        LocalDate checkInDate
    );
}
