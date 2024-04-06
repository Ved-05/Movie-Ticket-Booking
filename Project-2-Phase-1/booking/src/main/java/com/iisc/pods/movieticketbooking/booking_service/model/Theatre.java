package com.iisc.pods.movieticketbooking.booking_service.model;

/**
 * Resource representing a theatre in the system.
 */
public class Theatre {
    public final record Entity(
            Integer theatre_id,
            String name,
            String location
    ) {
    }

    public final record List(
            java.util.List<Entity> theatres
    ) {
    }

}
