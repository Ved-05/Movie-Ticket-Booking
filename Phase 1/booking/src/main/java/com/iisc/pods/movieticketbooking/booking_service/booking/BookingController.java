package com.iisc.pods.movieticketbooking.booking_service.booking;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Booking entity
 */
@RestController
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    /**
     * Get bookings by user id.
     *
     * @param user_id Id of the user
     * @return List of bookings for the user id with status code 200
     */
    @GetMapping("/users/{user_id}")
    public ResponseEntity<List<Booking>> getBookingById(@PathVariable Integer user_id) {
        ResponseEntity<List<Booking>> responseEntity;
        List<Booking> bookingsByUserId = bookingService.getBookingByUserId(user_id);
        responseEntity = new ResponseEntity<>(bookingsByUserId, HttpStatus.OK);
        return responseEntity;
    }

    /**
     * Create a new booking.
     *
     * @param booking Booking to be created
     * @return Created booking with status code 201 if created, else status code 400 for invalid request
     */
    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
        ResponseEntity<Booking> responseEntity;
        try {
            Booking savedBooking = bookingService.create(booking);
            responseEntity = new ResponseEntity<>(savedBooking, HttpStatus.CREATED);
        } catch (BadRequestException e) {
            responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return responseEntity;
    }

    /**
     * Delete bookings by user id.
     * @param user_id user id
     * @return status code 200 if deleted, else status code 404 for not found
     */
    @DeleteMapping("/users/{user_id}")
    public ResponseEntity<Booking> deleteBooking(@PathVariable Integer user_id) {
        ResponseEntity<Booking> responseEntity;
        try {
            bookingService.deleteByUserId(user_id);
            responseEntity = new ResponseEntity<>(HttpStatus.OK);
        } catch (BadRequestException e) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return responseEntity;
    }

    /**
     * Delete booking by user id and show id.
     * @param user_id user id
     * @param show_id show id
     * @return status code 200 if deleted, else status code 404 for not found
     */
    @DeleteMapping("/users/{user_id}/shows/{show_id}")
    public ResponseEntity<Booking> deleteBooking(@PathVariable Integer user_id, @PathVariable Integer show_id) {
        ResponseEntity<Booking> responseEntity;
        try {
            bookingService.deleteByUserIdAndShowId(user_id, show_id);
            responseEntity = new ResponseEntity<>(HttpStatus.OK);
        } catch (BadRequestException e) {
            responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return responseEntity;
    }

    /**
     * Delete all bookings of all users in all shows.
     * @return status code 200
     */
    @DeleteMapping
    public ResponseEntity<Booking> deleteAll() {
        ResponseEntity<Booking> responseEntity;
        try {
            bookingService.deleteAll();
            responseEntity = new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return responseEntity;
    }
}
