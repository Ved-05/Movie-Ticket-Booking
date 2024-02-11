package com.iisc.pods.movieticketbooking.booking_service.booking;

import com.iisc.pods.movieticketbooking.booking_service.show.Show;
import com.iisc.pods.movieticketbooking.booking_service.show.ShowService;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ShowService showService;

    @Autowired
    public BookingService(BookingRepository bookingRepository, ShowService showService) {
        this.bookingRepository = bookingRepository;
        this.showService = showService;
    }

    /**
     * Create a new booking.
     *
     * @param booking Booking object to be created.
     * @return Created booking object.
     * @throws BadRequestException If there is an issue serving the request.
     */
    public Booking create(Booking booking) throws BadRequestException {
        Integer show_id = booking.getShow().getShow_id();
        Show showById = showService.getShowById(show_id);
        if (showById == null) {
            throw new BadRequestException("Show " + show_id + " not found");
        } else if (showById.getSeats_available() < booking.getSeats_booked()) {
            throw new BadRequestException("Number of booked seats exceeds available seats for show " + show_id);
        }
        try {
            // TODD: Call wallet service to deduct amount
//            walletService.deductAmount(booking.getUser_id(), booking.getSeats_booked() * showById.getPrice());
            if (showById.getSeats_available() < booking.getSeats_booked())
                throw new BadRequestException("Wallet service not available");
        } catch (BadRequestException e) {
            throw new BadRequestException("Insufficient balance in wallet for user " + booking.getUser_id());
        }
        showById.setSeats_available(showById.getSeats_available() - booking.getSeats_booked());
        return bookingRepository.save(booking);
    }

    /**
     * Delete bookings by user id.
     *
     * @param userId user id
     */
    public void deleteByUserId(Integer userId) throws BadRequestException {
        Set<Integer> id = Collections.singleton(userId);
        List<Booking> allById = bookingRepository.findAllById(id);
        if (allById.isEmpty()) {
            throw new BadRequestException("Booking for user " + userId + " not found");
        }
        allById.forEach(booking -> {
            Show showById = showService.getShowById(booking.getShow().getShow_id());
            showById.setSeats_available(showById.getSeats_available() + booking.getSeats_booked());
            // TODO: Call wallet service to refund amount
        });
        bookingRepository.deleteAllById(id);
    }

    /**
     * Get booking by user id
     *
     * @param userId user id
     * @return booking object if found, null otherwise
     */
    public Booking getBookingByUserId(Integer userId) {
        return bookingRepository.findById(userId).orElse(null);
    }

    /**
     * Delete bookings by user id and show id.
     *
     * @param userId user id
     * @param showId show id
     */
    public void deleteByUserIdAndShowId(Integer userId, Integer showId) throws BadRequestException {
        List<Integer> ids = Arrays.asList(userId, showId);
        List<Booking> allById = bookingRepository.findAllById(ids);
        if (allById.isEmpty()) {
            throw new BadRequestException("Booking for user " + userId + " and show " + showId + " not found");
        }
        allById.forEach(booking -> {
            Show showById = showService.getShowById(booking.getShow().getShow_id());
            showById.setSeats_available(showById.getSeats_available() + booking.getSeats_booked());
            // TODO: Call wallet service to refund amount
        });
        bookingRepository.deleteAllById(ids);
    }

    /**
     * Delete all bookings. This will also reset all shows and wallets.
     */
    public void deleteAll() {
        showService.resetAllShows();
        // TODO: Call wallet service to reset all wallets
        bookingRepository.deleteAll();
    }
}
