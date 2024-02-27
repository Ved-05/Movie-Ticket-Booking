package com.iisc.pods.movieticketbooking.booking_service.theatre;

import com.iisc.pods.movieticketbooking.booking_service.show.Show;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;

/**
 * Resource representing a theatre in the system.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Theatre {
    @Id
    @GeneratedValue
    private Integer theatre_id;
    private String name;
    private String location;

    /**
     * Constructor for Theatre.
     *
     * @param name     Name of the theatre.
     * @param location Location of the theatre.
     */
    public Theatre(String name, String location) {
        this.name = name;
        this.location = location;
    }
}
