package com.liontech.resorts.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.liontech.resorts.domain.Booking;
import com.liontech.resorts.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findTopByBookingOrderByCreatedAtDesc(Booking booking);
}
