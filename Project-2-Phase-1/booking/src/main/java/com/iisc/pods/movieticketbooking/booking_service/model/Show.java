package com.iisc.pods.movieticketbooking.booking_service.model;

/**
 * Resource representing a show in the system.
 */
public final class Show implements ActorModel {
    Integer id;
    Theatre theatre;
    String title;
    Integer price;
    Integer seats_available;

    public Show(Integer id, Theatre theatre, String title, Integer price, Integer seats_available) {
        this.id = id;
        this.theatre = theatre;
        this.title = title;
        this.price = price;
        this.seats_available = seats_available;
    }

    public Integer id() {
        return id;
    }

    public Theatre theatre() {
        return theatre;
    }

    public String title() {
        return title;
    }

    public Integer price() {
        return price;
    }

    public Integer seats_available() {
        return seats_available;
    }

    public void bookSeats(Integer bookedSeats) {
        this.seats_available -= bookedSeats;
    }
}
