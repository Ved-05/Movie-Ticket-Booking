package com.iisc.pods.movieticketbooking.booking_service.theatre;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for Booking entity
 */
@RestController
public class TheatreController {
    private final TheatreService theatreService;

    @Autowired
    public TheatreController(TheatreService theatreService) {
        this.theatreService = theatreService;
    }

    @GetMapping("/theatres")
    public ResponseEntity<List<Theatre>> getAllTheatres() {
        return new ResponseEntity<>(theatreService.getAllTheatres(), HttpStatus.OK);
    }
}
