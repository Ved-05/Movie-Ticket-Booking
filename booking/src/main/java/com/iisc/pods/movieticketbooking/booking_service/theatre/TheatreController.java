package com.iisc.pods.movieticketbooking.booking_service.theatre;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for Booking entity
 */
@RequestMapping("/theatres")
@RestController
public class TheatreController {
    @Autowired
    private TheatreService theatreService;

    /**
     * Get all theatres
     *
     * @return List of all theatres with status code 200
     */
    @GetMapping
    public ResponseEntity<List<Theatre>> getAllTheatres() {
        return new ResponseEntity<>(theatreService.getAllTheatres(), HttpStatus.OK);
    }
}
