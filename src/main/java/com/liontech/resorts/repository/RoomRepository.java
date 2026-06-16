package com.liontech.resorts.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.liontech.resorts.domain.Room;
import com.liontech.resorts.domain.RoomStatus;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findAllByStatusOrderByNightlyRateAsc(RoomStatus status);

    List<Room> findAllByOrderByRoomNumberAsc();

    Optional<Room> findByRoomNumberIgnoreCase(String roomNumber);
}
