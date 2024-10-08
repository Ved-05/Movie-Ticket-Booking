package com.iisc.pods.movieticketbooking.booking_service.show;

import com.iisc.pods.movieticketbooking.booking_service.theatre.TheatreNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for Booking entity
 */
@Slf4j
@RestController
@RequestMapping("/shows")
public class ShowController {

    @Autowired
    private ShowService showService;

    @GetMapping("/theatres/{theatreId}")
    public ResponseEntity<List<Show>> getAllShowsByTheatreId(@PathVariable Integer theatreId) {
        ResponseEntity<List<Show>> responseEntity;
        try {
            List<Show> allShowsByTheatreId = showService.getAllShowsByTheatreId(theatreId);
            responseEntity = new ResponseEntity<>(allShowsByTheatreId, HttpStatus.OK);
        } catch (TheatreNotFoundException e) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return responseEntity;
    }

    @GetMapping("/{showId}")
    public ResponseEntity<Show> getShowById(@PathVariable Integer showId) throws ShowNotFoundException {
        ResponseEntity<Show> responseEntity;
        try {
            Show showById = showService.getShowById(showId);
            responseEntity = new ResponseEntity<>(showById, HttpStatus.OK);
        } catch (ShowNotFoundException e) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return responseEntity;
    }
}
