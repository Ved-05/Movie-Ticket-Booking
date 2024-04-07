package com.iisc.pods.movieticketbooking.booking_service.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.*;
import com.iisc.pods.movieticketbooking.booking_service.BookingRoutes;
import com.iisc.pods.movieticketbooking.booking_service.model.ActorModel;
import com.iisc.pods.movieticketbooking.booking_service.model.Booking;
import com.iisc.pods.movieticketbooking.booking_service.model.Show;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class ShowActor extends AbstractBehavior<ShowActor.Request> {
    private final static Logger log = Logger.getLogger(BookingRoutes.class.getName());

    public sealed interface Request {
    }

    public record GetShow(ActorRef<ActorModel> replyTo) implements Request {
    }

    private final Show show;
    private final List<Booking> bookings;

    private ShowActor(ActorContext<Request> context, Show show) {
        super(context);
        this.show = show;
        this.bookings = new ArrayList<>();
    }

    /**
     * Create a new instance of the actor
     *
     * @return Behavior of the actor
     */
    public static Behavior<Request> create(Show show) {
        return Behaviors.setup(context -> new ShowActor(context, show));
    }

    @Override
    public Receive<Request> createReceive() {
        return newReceiveBuilder()
                .onMessage(GetShow.class, this::onGetShow)
                .onMessage(CreateBooking.class, this::onCreateBooking)
                .onMessage(GetBookingForUser.class, this::onGetBookingForUser)
                .onMessage(DeleteAllBookings.class, this::onDeleteAllBookings)
                .onMessage(DeleteBookingForUser.class, this::onDeleteBookingForUser)
                .build();
    }

    private Behavior<Request> onGetShow(GetShow getShow) {
        log.info("Received request to get show details for id : " + this.show.id());
        getShow.replyTo.tell(this.show);
        return this;
    }

    public record CreateBooking(Booking booking, ActorRef<BookingActor.Request> respondTo) implements Request {
    }

    /**
     * Create a new booking for the show
     *
     * @param bookingRequest Request object
     * @return Behavior of the actor
     */
    private Behavior<Request> onCreateBooking(CreateBooking bookingRequest) {
        log.info("Received request to create booking for show id : " + this.show.id()
                + " and user id : " + bookingRequest.booking.user_id());
        BookingActor.ActionResponse actionResponse;
        Integer requiredAmount = bookingRequest.booking.seats_booked() * this.show.price();
        if (this.show.seats_available() < bookingRequest.booking.seats_booked()) {
            actionResponse = new BookingActor.ActionFailed("Failed. Number of booked seats exceeds available seats" +
                    " for show " + this.show.id());
        } else if (!updateWallet(bookingRequest.booking.user_id(), requiredAmount, false)) {
            actionResponse = new BookingActor.ActionFailed("Failed. Amount exceeds 1000");
        } else {
            this.show.bookSeats(bookingRequest.booking.seats_booked());
            bookings.add(bookingRequest.booking);
            actionResponse = new BookingActor.ActionPerformed("Success");
        }
        bookingRequest.respondTo.tell(actionResponse);
        return this;
    }

    /**
     * Updates the required amount from/to the user's wallet
     *
     * @param userId         User id of the user
     * @param requiredAmount Amount to be deducted
     * @return true if amount is updated successfully, false otherwise
     */
    private boolean updateWallet(Integer userId, Integer requiredAmount, boolean isRefund) {
        AtomicBoolean success = new AtomicBoolean(false);
        ActorSystem<Void> actorSystem = getContext().getSystem();
        String url = "http://localhost:8080/wallet/deduct/" + userId;
        String action = isRefund ? "credit" : "debit";

        HttpEntity.Strict entity = HttpEntities.create(
                MediaTypes.APPLICATION_JSON.toContentType(),
                "{\"action\": \"" + action + "\", \"amount\": " + requiredAmount + "}");
        HttpRequest httpRequest = HttpRequest.create()
                .withUri(url)
                .withMethod(HttpMethods.PUT)
                .withEntity(entity);

        Http.get(actorSystem)
                .singleRequest(httpRequest)
                .thenAccept(response -> {
                    log.info("ActionResponse from wallet service : " + response.status());
                    if (response.status().isSuccess()) {
                        log.info("Amount " + action + "ed successfully");
                        success.set(true);
                    } else {
                        log.info("Amount could not be deducted from wallet");
                    }
                });

        return success.get();
    }

    public record GetBookingForUser(Integer userId, ActorRef<List<Booking>> respondTo) implements Request {
    }

    /**
     * Get booking details for the user
     *
     * @param getBookingForUser Request object
     * @return Behavior of the actor
     */
    private Behavior<Request> onGetBookingForUser(GetBookingForUser getBookingForUser) {
        log.info("Received request to get booking details for user id : " + getBookingForUser.userId());
        List<Booking> bookingsForUser = bookings.stream()
                .filter(booking -> booking.user_id().equals(getBookingForUser.userId()))
                .toList();
        getBookingForUser.respondTo.tell(bookingsForUser);
        return this;
    }

    public record DeleteAllBookings(ActorRef<BookingActor.ActionResponse> replyTo) implements Request {
    }

    /**
     * Deletes all bookings for the show.
     *
     * @param request Request object
     * @return Behavior of the actor
     */
    private Behavior<Request> onDeleteAllBookings(DeleteAllBookings request) {
        log.info("Deleting all bookings for show " + this.show.id());
        try {
            bookings.forEach(this::revertBooking);
        } catch (RuntimeException e) {
            request.replyTo.tell(new BookingActor.ActionFailed("Failed"));
            return this;
        }
        return this;
    }

    /**
     * Reverts the booking by updating the show's available seats and refunding the amount to the user
     *
     * @param booking Booking object to be reverted
     */
    private void revertBooking(Booking booking) {
        if (!updateWallet(booking.user_id(), booking.seats_booked() * this.show.price(), true)) {
            log.info("Amount could not be refunded to wallet for user " + booking.user_id());
            throw new RuntimeException("Amount could not be refunded to wallet for user " + booking.user_id());
        }
        this.show.revertBookedSeats(this.show.seats_available() + booking.seats_booked());
        this.bookings.remove(booking);
    }

    public record DeleteBookingForUser(Integer userId, ActorRef<BookingActor.ActionResponse> respondTo) implements Request {
    }

    /**
     * Deletes all bookings for the user.
     *
     * @param request Request object
     * @return Behavior of the actor
     */
    private Behavior<Request> onDeleteBookingForUser(DeleteBookingForUser request) {
        log.info("Deleting all bookings for user " + request.userId());
        List<Booking> bookingsForUser = bookings.stream()
                .filter(booking -> booking.user_id().equals(request.userId()))
                .toList();
        try {
            bookingsForUser.forEach(this::revertBooking);
        } catch (RuntimeException e) {
            request.respondTo.tell(new BookingActor.ActionFailed("Failed"));
            return this;
        }
        request.respondTo.tell(new BookingActor.ActionPerformed("Success"));
        return this;
    }
}