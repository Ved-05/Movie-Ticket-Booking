package com.iisc.pods.movieticketbooking.booking_service.model;

/**
 * Resource representing a booking in the system.
 */
public record NotFoundMessage(
        String message
) implements ActorModel {
}
