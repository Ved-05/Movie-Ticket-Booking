package com.iisc.pods.movieticketbooking.booking_service.booking;

import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Booking entity
 */
@RestController
public class BookingController {
    private final BookingService bookingService;

    /**
     * Constructor for BookingController
     *
     * @param bookingService BookingService
     */
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/bookings/users/{user_id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Integer user_id) {
        ResponseEntity<Booking> responseEntity;
        Booking savedBooking = bookingService.getBookingByUserId(user_id);
        responseEntity = new ResponseEntity<>(savedBooking, HttpStatus.CREATED);
        return responseEntity;
    }

    @PostMapping("/bookings")
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

    @DeleteMapping("/bookings/users/{user_id}")
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

    @DeleteMapping("/bookings/users/{user_id}/shows/{show_id}")
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

    @DeleteMapping("/bookings")
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
