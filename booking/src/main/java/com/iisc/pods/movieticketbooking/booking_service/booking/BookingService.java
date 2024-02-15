package com.iisc.pods.movieticketbooking.booking_service.booking;

import com.iisc.pods.movieticketbooking.booking_service.show.Show;
import com.iisc.pods.movieticketbooking.booking_service.show.ShowService;
import com.iisc.pods.movieticketbooking.booking_service.utils.WalletServiceByRest;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShowService showService;

    @Autowired
    private WalletServiceByRest walletServiceByRest;

    /**
     * Create a new booking.
     *
     * @param booking Booking object to be created.
     * @return Created booking object.
     * @throws BadRequestException If there is an issue serving the request.
     */
    public Booking create(Booking booking) throws BadRequestException {
        Integer show_id = booking.getShow_id();
        Show showById = showService.getShowById(show_id);
        if (showById == null)
            throw new BadRequestException("Show " + show_id + " not found.");
        else if (showById.getSeats_available() < booking.getSeats_booked())
            throw new BadRequestException("Number of booked seats exceeds available seats for show " + show_id);

        boolean isAmountDeducted = walletServiceByRest.deductAmountFromWallet(booking.getUser_id(),
                booking.getSeats_booked() * showById.getPrice());
        if (!isAmountDeducted) throw new BadRequestException("Amount could not be deducted from wallet.");
        showById.setSeats_available(showById.getSeats_available() - booking.getSeats_booked());
        return bookingRepository.save(booking);
    }

    /**
     * Delete bookings by user id.
     *
     * @param userId user id
     */
    public void deleteByUserId(Integer userId) throws BadRequestException {
        List<Booking> bookingsByUserId = bookingRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Booking for user " + userId + " not found."));

        deleteBookingRecords(bookingsByUserId);
    }

    /**
     * Delete bookings
     *
     * @param bookings List of bookings
     */
    private void deleteBookingRecords(List<Booking> bookings) throws BadRequestException {
        for (Booking booking : bookings) {
            showService.updateShowSeats(booking.getShow_id(), booking.getSeats_booked());
            Show show = showService.getShowById(booking.getShow_id());
            boolean isAmountRefunded = walletServiceByRest.refundAmount(booking.getUser_id(),
                    booking.getSeats_booked() * show.getPrice());
            if (!isAmountRefunded)
                throw new BadRequestException("Amount could not be refunded to wallet for user " + booking.getUser_id());
        }

        bookingRepository.deleteAllById(bookings.stream().map(Booking::getId).collect(Collectors.toList()));
    }

    /**
     * Get bookings by user id
     *
     * @param userId user id
     * @return List of bookings for user id if found, else empty list
     */
    public List<Booking> getBookingByUserId(Integer userId) {
        return bookingRepository.findByUserId(userId).orElse(Collections.emptyList());
    }

    /**
     * Delete bookings by user id and show id.
     *
     * @param userId user id
     * @param showId show id
     */
    public void deleteByUserIdAndShowId(Integer userId, Integer showId) throws BadRequestException {
        List<Booking> bookingsByUserAndShowId = bookingRepository.findByUserIdAndShowId(userId, showId)
                .orElseThrow(() -> new BadRequestException("Booking for user " + userId + " & show " + showId + " not found."));

        deleteBookingRecords(bookingsByUserAndShowId);
    }

    /**
     * Delete all bookings from the repository and refund the amount to the users.
     */
    public void deleteAll() throws BadRequestException {
        List<Booking> bookingList = bookingRepository.findAll();
        deleteBookingRecords(bookingList);
    }
}
