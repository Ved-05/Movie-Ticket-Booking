package com.iisc.pods.movieticketbooking.booking_service.model;

/**
 * Resource representing a show in the system.
 */
public record Show(
        Integer id,
        Theatre theatre,
        String title,
        Integer price,
        Integer seats_available
) {
}
