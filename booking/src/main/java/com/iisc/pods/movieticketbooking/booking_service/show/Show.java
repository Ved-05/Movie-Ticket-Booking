package com.iisc.pods.movieticketbooking.booking_service.show;

import com.iisc.pods.movieticketbooking.booking_service.theatre.Theatre;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Resource representing a movie show in the system.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Show {
    @Id
    @GeneratedValue
    private Integer show_id;
    @ManyToOne
    @JoinColumn(name = "theatre_id")
    private Theatre theatre;
    private String title;
    private Integer price;
    private Integer seats_available;

    /**
     * Constructor for Show.
     *
     * @param theatre         Foreign key to the theatre.
     * @param title           Title of the movie.
     * @param price           Price of the movie.
     * @param seats_available Number of seats available for the show.
     */
    public Show(Theatre theatre, String title, Integer price, Integer seats_available) {
        this.theatre = theatre;
        this.title = title;
        this.price = price;
        this.seats_available = seats_available;
    }

    @Override
    public String toString() {
        return "Show{" +
                "id=" + show_id +
                ", theatre=" + theatre +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", seats_available=" + seats_available +
                '}';
    }
}
