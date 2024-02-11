package com.iisc.pods.movieticketbooking.booking_service.booking;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for Booking
 */
public interface BookingRepository extends JpaRepository<Booking, Integer> {
}
