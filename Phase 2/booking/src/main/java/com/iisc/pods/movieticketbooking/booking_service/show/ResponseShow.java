package com.iisc.pods.movieticketbooking.booking_service.show;

import com.iisc.pods.movieticketbooking.booking_service.theatre.Theatre;
import jakarta.persistence.*;
import lombok.*;


/**
 * Resource representing a movie show response in the system.
 */
@Getter
@Setter
@AllArgsConstructor
@ToString
public class ResponseShow {
    private Integer id;
    private Integer theatre_id;
    private String title;
    private Integer price;
    private Integer seats_available;
}
