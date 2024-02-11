package com.iisc.pods.movieticketbooking.booking_service.theatre;

import com.iisc.pods.movieticketbooking.booking_service.show.Show;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;

/**
 * Resource representing a theatre in the system.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Theatre {
    private @Id
    @GeneratedValue Integer theatre_id;
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

    @Override
    public String toString() {
        return "Theatre{" +
                "id=" + theatre_id +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}
