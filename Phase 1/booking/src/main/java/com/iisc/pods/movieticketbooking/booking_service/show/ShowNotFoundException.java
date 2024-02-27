package com.iisc.pods.movieticketbooking.booking_service.show;

public class ShowNotFoundException extends RuntimeException {
    public ShowNotFoundException(Integer id) {
        super("Could not find show " + id);
    }
}
