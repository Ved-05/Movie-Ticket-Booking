package com.iisc.pods.movieticketbooking.booking_service.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.iisc.pods.movieticketbooking.booking_service.BookingRoutes;
import com.iisc.pods.movieticketbooking.booking_service.model.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class BookingActor extends AbstractBehavior<BookingActor.Command> {
    private final static Logger log = Logger.getLogger(BookingRoutes.class.getName());

    public sealed interface Command {
    }

    public final static record GetShowsByTheatreId(Integer theatreId, ActorRef<Show.List> replyTo) implements Command {
    }

    public final static record GetShowById(Integer showId, ActorRef<Show.Entity> replyTo) implements Command {
    }

    public final static record GetBookingsByUser(Integer userId, ActorRef<Booking.List> replyTo) implements Command {
    }

    public final static record CreateBooking(Booking.Entity booking, ActorRef<ActionPerformed> replyTo) implements Command {
    }

    public final static record DeleteBookingByUser(Integer userId, ActorRef<ActionPerformed> replyTo) implements Command {
    }

    public final static record DeleteBookingByShowAndUserId(Integer userId, Integer showId, ActorRef<ActionPerformed> replyTo) implements Command {
    }

    public final static record DeleteAllBookings(ActorRef<ActionPerformed> replyTo) implements Command {
    }

    public final static record GetTheatres(ActorRef<Theatre.List> replyTo) implements Command {
    }

    public final static record ActionPerformed(String description) implements Command {
    }

    private final Theatre.List theatres;

    private BookingActor(ActorContext<Command> context) {
        super(context);
        theatres = new Theatre.List(loadTheatreFromJson());
    }

    /**
     * Load theatres from CSV file
     *
     * @return List of theatres
     */
    private List<Theatre.Entity> loadTheatreFromJson() {
        log.info("Loading theatres from CSV file");
        List<Theatre.Entity> theatres = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("data/theatres.csv"));
            br.readLine();
            String line = br.readLine();
            while (line != null) {
                String[] values = line.split(",");
                Integer theatreId = Integer.parseInt(values[0]);
                String name = values[1];
                String location = values[2];
                Theatre.Entity theatre = new Theatre.Entity(theatreId, name, location);
                theatres.add(theatre);
                line = br.readLine();
            }
        } catch (IOException e) {
            log.info("Error loading theatres from CSV file. Message: " + e.getMessage());
        }
        return null;
    }

    /**
     * Create a new instance of the actor
     *
     * @return Behavior of the actor
     */
    public static Behavior<Command> create() {
        return Behaviors.setup(BookingActor::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(GetTheatres.class, this::onGetTheatres)
                .onMessage(GetShowsByTheatreId.class, this::onGetShowsByTheatreId)
                .onMessage(GetShowById.class, this::onGetShowById)
                .onMessage(GetBookingsByUser.class, this::onGetBookingsByUser)
                .onMessage(CreateBooking.class, this::onCreateBooking)
                .onMessage(DeleteBookingByUser.class, this::onDeleteBookingByUser)
                .onMessage(DeleteBookingByShowAndUserId.class, this::onDeleteBookingByShowAndUserId)
                .onMessage(DeleteAllBookings.class, this::onDeleteAllBookings)
                .build();
    }

    // TODO: Implement below methods.
    private Behavior<Command> onDeleteAllBookings(DeleteAllBookings deleteAllBookings) {
        return null;
    }

    private Behavior<Command> onDeleteBookingByShowAndUserId(DeleteBookingByShowAndUserId deleteBookingByShowAndUserId) {
        return null;
    }

    private Behavior<Command> onDeleteBookingByUser(DeleteBookingByUser deleteBookingByUser) {
        return null;
    }

    private Behavior<Command> onCreateBooking(CreateBooking createBooking) {
        return null;
    }

    private Behavior<Command> onGetBookingsByUser(GetBookingsByUser getBookingsByUser) {
        return null;
    }

    private Behavior<Command> onGetShowById(GetShowById getShowById) {
        return null;
    }

    private Behavior<Command> onGetShowsByTheatreId(GetShowsByTheatreId getShowsByTheatreId) {
        return null;
    }

    private Behavior<Command> onGetTheatres(GetTheatres getTheatres) {
        return null;
    }

}