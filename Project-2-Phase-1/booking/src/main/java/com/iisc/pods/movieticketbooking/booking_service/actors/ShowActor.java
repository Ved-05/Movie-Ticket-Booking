package com.iisc.pods.movieticketbooking.booking_service.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import com.iisc.pods.movieticketbooking.booking_service.BookingRoutes;
import com.iisc.pods.movieticketbooking.booking_service.model.ActorModel;
import com.iisc.pods.movieticketbooking.booking_service.model.Booking;
import com.iisc.pods.movieticketbooking.booking_service.model.Show;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
                .build();
    }

    private Behavior<Request> onGetShow(GetShow getShow) {
        log.info("Received request to get show details for id : " + this.show.id());
        getShow.replyTo.tell(this.show);
        return this;
    }

    public record CreateBooking(Booking booking, ActorRef<BookingActor.Request> respondTo) implements Request {
    }

    private Behavior<Request> onCreateBooking(CreateBooking bookingRequest) {
        log.info("Received request to create booking for show id : " + this.show.id()
                + " and user id : " + bookingRequest.booking.user_id());
        BookingActor.Response response;
        Integer requiredAmount = bookingRequest.booking.seats_booked() * this.show.price();
        if (this.show.seats_available() < bookingRequest.booking.seats_booked()) {
            response = new BookingActor.ActionFailed("Failed. Number of booked seats exceeds available seats" +
                    " for show " + this.show.id());
        } else if (!deductFromWallet(bookingRequest.booking.user_id(), requiredAmount)) {
            response = new BookingActor.ActionFailed("Failed. Amount exceeds 1000");
        } else {
            this.show.bookSeats(bookingRequest.booking.seats_booked());
            bookings.add(bookingRequest.booking);
            response = new BookingActor.ActionPerformed("Success");
        }
        bookingRequest.respondTo.tell(response);
        return this;
    }

    /**
     * Deducts the required amount from the user's wallet
     *
     * @param userId         User id of the user
     * @param requiredAmount Amount to be deducted
     * @return true if amount is deducted successfully, false otherwise
     */
    private boolean deductFromWallet(Integer userId, Integer requiredAmount) {
        ActorSystem<Void> actorSystem = getContext().getSystem();
        String url = "http://localhost:8080/wallet/deduct/" + userId + "/" + requiredAmount;
        AtomicBoolean success = new AtomicBoolean(false);
        Http.get(actorSystem)
                .singleRequest(HttpRequest.create(url))
                .thenAccept(response -> {
                    log.info("Response from wallet service : " + response.status());
                    if (response.status().isSuccess()) {
                        log.info("Amount deducted successfully");
                        success.set(true);
                    } else {
                        log.info("Amount could not be deducted from wallet");
                    }
                });

        return success.get();
    }

    public record GetBookingForUser(Integer userId, ActorRef<List<Booking>> respondTo) implements Request {
    }

    private Behavior<Request> onGetBookingForUser(GetBookingForUser getBookingForUser) {
        log.info("Received request to get booking details for user id : " + getBookingForUser.userId());
        List<Booking> bookingsForUser = bookings.stream()
                .filter(booking -> booking.user_id().equals(getBookingForUser.userId()))
                .toList();
        getBookingForUser.respondTo.tell(bookingsForUser);
        return this;
    }
}