package com.iisc.pods.movieticketbooking.booking_service;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Scheduler;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import com.iisc.pods.movieticketbooking.booking_service.actors.BookingActor;
import com.iisc.pods.movieticketbooking.booking_service.model.Booking;
import com.iisc.pods.movieticketbooking.booking_service.model.Show;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

import static akka.http.javadsl.server.Directives.*;

/**
 * Routes can be defined in separated classes like shown in here
 */
public class BookingRoutes {
    private final static Logger log = Logger.getLogger(BookingRoutes.class.getName());

    private final ActorRef<BookingActor.Command> bookingActor;

    private final Duration askTimeout;

    private final Scheduler scheduler;

    /**
     * Constructor for BookingRoutes
     *
     * @param system       ActorSystem
     * @param bookingActor ActorRef for BookingActor
     */
    public BookingRoutes(ActorSystem<?> system, ActorRef<BookingActor.Command> bookingActor) {
        this.bookingActor = bookingActor;
        this.scheduler = system.scheduler();
        this.askTimeout = system.settings().config().getDuration("my-app.routes.ask-timeout");
    }

    /**
     * Route for booking service
     *
     * @return Route
     */
    public Route bookingServiceRoute() {
        return concat(
                path("theatres", () ->
                        get(() ->
                                onSuccess(
                                        AskPattern.ask(bookingActor, BookingActor.GetTheatres::new, askTimeout, scheduler),
                                        theatres -> complete(StatusCodes.OK, theatres, Jackson.marshaller())
                                )
                        )
                ),
                pathPrefix("shows", () ->
                        concat(
                                pathPrefix("theatres", () ->
                                        path(PathMatchers.segment(), theatreId ->
                                                get(() ->
                                                        onSuccess(getShowsForTheatre(theatreId), shows -> {
                                                            if (shows.shows().isEmpty()) {
                                                                log.info("No shows found for theatre: " + theatreId);
                                                                return complete(StatusCodes.NOT_FOUND,
                                                                        "No shows found for theatre",
                                                                        Jackson.marshaller());
                                                            } else {
                                                                log.info("Shows found for theatre: " + theatreId);
                                                                return complete(StatusCodes.OK, shows, Jackson.marshaller());
                                                            }
                                                        })
                                                )
                                        )
                                ),
                                path(PathMatchers.segment(), showId ->
                                        get(() ->
                                                onSuccess(getShowById(showId),
                                                        show -> {
                                                            if (show == null) {
                                                                log.info("Show not found: " + showId);
                                                                return complete(StatusCodes.NOT_FOUND, "Show not found", Jackson.marshaller());
                                                            } else {
                                                                log.info("Show found: " + showId);
                                                                return complete(StatusCodes.OK, show, Jackson.marshaller());
                                                            }
                                                        }
                                                )
                                        )
                                ),
                                pathPrefix("bookings", () ->
                                        concat(
                                                path("users", () ->
                                                        path(PathMatchers.segment(), userId ->
                                                                get(() ->
                                                                        onSuccess(getBookingsForUser(userId),
                                                                                bookings -> {
                                                                                    log.info("Bookings found for user: " + userId);
                                                                                    return complete(StatusCodes.OK, bookings, Jackson.marshaller());
                                                                                }
                                                                        )
                                                                )
                                                        )
                                                ),

                                                pathEnd(() ->
                                                        post(() ->
                                                                entity(
                                                                        Jackson.unmarshaller(Booking.Entity.class),
                                                                        bookingRequest ->
                                                                                onSuccess(createBooking(bookingRequest),
                                                                                        actionPerformed -> {
                                                                                            if (actionPerformed.description().equals("Success")) {
                                                                                                log.info("Booking successful");
                                                                                                return complete(StatusCodes.OK, "Booking successful", Jackson.marshaller());
                                                                                            } else {
                                                                                                log.info("Booking failed");
                                                                                                return complete(StatusCodes.BAD_REQUEST, "Booking failed", Jackson.marshaller());
                                                                                            }
                                                                                        }
                                                                                )
                                                                )
                                                        )
                                                ),

                                                path("users", () ->
                                                        path(PathMatchers.segment(), userId ->
                                                                delete(() ->
                                                                        onSuccess(deleteAllBookingsForUser(userId), actionPerformed -> {
                                                                            log.info("Bookings deletion status: " + actionPerformed.description());
                                                                            if (actionPerformed.description().equals("Success")) {
                                                                                return complete(StatusCodes.OK, "Bookings deleted successfully", Jackson.marshaller());
                                                                            } else {
                                                                                return complete(StatusCodes.NOT_FOUND, "User not found", Jackson.marshaller());
                                                                            }
                                                                        })
                                                                )
                                                        )
                                                ),

                                                path("users", () ->
                                                        path(PathMatchers.segment().
                                                                slash("shows").
                                                                slash(PathMatchers.segment()), (userId, showId) ->
                                                                delete(() ->
                                                                        onSuccess(deleteBookingsForUserInShow(userId, showId), actionPerformed -> {
                                                                            log.info("Bookings deletion status: " + actionPerformed.description());
                                                                            if (actionPerformed.description().equals("Success")) {
                                                                                return complete(StatusCodes.OK, "Bookings deleted successfully", Jackson.marshaller());
                                                                            } else {
                                                                                return complete(StatusCodes.NOT_FOUND, "User or show not found", Jackson.marshaller());
                                                                            }
                                                                        })
                                                                )
                                                        )
                                                ),

                                                delete(() ->
                                                        onSuccess(deleteAllBookings(), actionPerformed -> {
                                                            log.info("All bookings deleted successfully");
                                                            return complete(StatusCodes.OK, "All bookings deleted successfully", Jackson.marshaller());
                                                        })
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private CompletionStage<BookingActor.ActionPerformed> deleteAllBookings() {
        return AskPattern.ask(bookingActor, BookingActor.DeleteAllBookings::new, askTimeout, scheduler);
    }

    private CompletionStage<BookingActor.ActionPerformed> deleteBookingsForUserInShow(String userId, String showId) {
        log.info("Deleting bookings for user: " + userId + " in show: " + showId);
        return AskPattern.ask(bookingActor, ref -> new BookingActor.DeleteBookingByShowAndUserId(
                Integer.parseInt(userId), Integer.parseInt(showId), ref), askTimeout, scheduler);
    }

    private CompletionStage<BookingActor.ActionPerformed> deleteAllBookingsForUser(String userId) {
        log.info("Deleting bookings for user: " + userId);
        return AskPattern.ask(bookingActor,
                ref -> new BookingActor.DeleteBookingByUser(Integer.parseInt(userId), ref), askTimeout, scheduler);
    }

    private CompletionStage<BookingActor.ActionPerformed> createBooking(Booking.Entity bookingRequest) {
        log.info("Creating booking: " + bookingRequest.toString());
        return AskPattern.ask(bookingActor, ref -> new BookingActor.CreateBooking(bookingRequest, ref),
                askTimeout, scheduler);
    }

    private CompletionStage<Booking.List> getBookingsForUser(String userId) {
        log.info("Fetching bookings for user: " + userId);
        return AskPattern.ask(bookingActor, ref -> new BookingActor.GetBookingsByUser(Integer.parseInt(userId), ref),
                askTimeout, scheduler);
    }

    private CompletionStage<Show.Entity> getShowById(String showId) {
        log.info("Fetching show: " + showId);
        return AskPattern.ask(bookingActor, ref -> new BookingActor.GetShowById(Integer.parseInt(showId), ref),
                askTimeout, scheduler);
    }

    private CompletionStage<Show.List> getShowsForTheatre(String theatreId) {
        log.info("Fetching shows for theatre: " + theatreId);
        return AskPattern.ask(bookingActor, ref -> new BookingActor.GetShowsByTheatreId(Integer.parseInt(theatreId), ref),
                askTimeout, scheduler);
    }
}
