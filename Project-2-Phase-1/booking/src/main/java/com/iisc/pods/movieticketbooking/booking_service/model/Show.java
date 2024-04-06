package com.iisc.pods.movieticketbooking.booking_service.model;

/**
 * Resource representing a show in the system.
 */
public class Show {
    public final record Entity(
            Integer id,
            Theatre theatre,
            String title,
            Integer price,
            Integer seats_available
    ) {
    }

    public final record List(
            java.util.List<Entity> shows
    ) {
    }

}
