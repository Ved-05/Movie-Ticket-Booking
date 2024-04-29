package com.iisc.pods.movieticketbooking.booking_service.model;

/**
 * Resource representing a theatre in the system.
 */
public record Theatre(
        Integer theatre_id,
        String name,
        String location
) {
}
