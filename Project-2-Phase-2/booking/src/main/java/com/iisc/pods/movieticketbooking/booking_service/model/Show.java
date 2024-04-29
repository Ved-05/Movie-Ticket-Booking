package com.iisc.pods.movieticketbooking.booking_service.model;

/**
 * Resource representing a show in the system.
 */
public record Show (
    Integer id,
    Integer theatre_id,
    String title,
    Integer price,
    Integer seats_available) implements ActorModel {

    public Show bookSeats(Integer bookedSeats) {
        return new Show(id, theatre_id, title, price, this.seats_available - bookedSeats);
    }

    public Show revertBookedSeats(Integer bookedSeats) {
        return new Show(id, theatre_id, title, price, this.seats_available + bookedSeats);
    }
}
