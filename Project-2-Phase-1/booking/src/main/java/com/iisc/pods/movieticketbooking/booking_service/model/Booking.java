package com.iisc.pods.movieticketbooking.booking_service.model;

/**
 * Resource representing a booking in the system.
 */
public record Booking(
        Integer id,
        Integer show_id,
        Integer user_id,
        Integer seats_booked
) {
}
