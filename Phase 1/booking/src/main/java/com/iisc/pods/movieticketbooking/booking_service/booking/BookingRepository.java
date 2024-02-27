package com.iisc.pods.movieticketbooking.booking_service.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Booking
 */
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    /**
     * Find booking by user id.
     *
     * @param user_id user id
     * @return Booking object
     */
    @Query("SELECT b FROM Booking b WHERE b.user_id = :user_id")
    Optional<List<Booking>> findByUserId(Integer user_id);


    /**
     * Find bookings by user id and show id.
     *
     * @param user_id user id
     * @param show_id show id
     */
    @Query("SELECT b FROM Booking b WHERE b.user_id = :user_id AND b.show_id = :show_id")
    Optional<List<Booking>> findByUserIdAndShowId(Integer user_id, Integer show_id);
}
