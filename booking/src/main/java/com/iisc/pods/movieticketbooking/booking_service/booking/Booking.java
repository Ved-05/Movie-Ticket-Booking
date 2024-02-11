package com.iisc.pods.movieticketbooking.booking_service.booking;

import com.iisc.pods.movieticketbooking.booking_service.show.Show;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Resource representing a booking in the system.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Booking {
    private @Id @GeneratedValue Integer id;
    private @ManyToOne @JoinColumn(name = "show_id") Show show;
    private Integer user_id;
    private Integer seats_booked;

    /**
     * Constructor for Booking.
     *
     * @param show         Foreign key to the show.
     * @param user_id      Foreign key to the user.
     * @param seats_booked Number of seats booked.
     */
    public Booking(Show show, Integer user_id, Integer seats_booked) {
        this.show = show;
        this.user_id = user_id;
        this.seats_booked = seats_booked;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", show=" + show +
                ", user_id=" + user_id +
                ", seats_booked=" + seats_booked +
                '}';
    }
}
