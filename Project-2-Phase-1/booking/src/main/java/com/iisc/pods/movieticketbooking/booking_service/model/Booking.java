package com.iisc.pods.movieticketbooking.booking_service.model;

/**
 * Resource representing a booking in the system.
 */
public class Booking {
    public final record Entity(
            Integer id,
            Integer show_id,
            Integer user_id,
            Integer seats_booked
    ) {
    }

    public final record List(
            java.util.List<Entity> theatres
    ) {
    }

}
