package com.iisc.pods.movieticketbooking.booking_service.theatre;

public class TheatreNotFoundException extends RuntimeException {
    public TheatreNotFoundException(Integer id) {
        super("Could not find theatre " + id);
    }
}
