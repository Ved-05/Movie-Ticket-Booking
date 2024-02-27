package com.iisc.pods.movieticketbooking.booking_service.booking;

import com.iisc.pods.movieticketbooking.booking_service.show.Show;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Resource representing a booking in the system.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Booking {
    @Id
    @GeneratedValue
    private Integer id;
    private Integer show_id;
    private Integer user_id;
    private Integer seats_booked;

    /**
     * Constructor for Booking.
     *
     * @param show         Foreign key to the show.
     * @param user_id      Foreign key to the user.
     * @param seats_booked Number of seats booked.
     */
    public Booking(Integer show_id, Integer user_id, Integer seats_booked) {
        this.show_id = show_id;
        this.user_id = user_id;
        this.seats_booked = seats_booked;
    }
}