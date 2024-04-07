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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class BookingActor extends AbstractBehavior<BookingActor.Request> {
    private final static Logger log = Logger.getLogger(BookingRoutes.class.getName());

    private final Map<Integer, Theatre> theatres;
    private final Map<Integer, ActorRef<ShowActor.Request>> showActors;

    public sealed interface Request {
    }

    public record GetShowsByTheatreId(Integer theatreId, ActorRef<List<Show>> replyTo) implements Request {
    }

    public record GetShowById(Integer showId, ActorRef<ActorModel> replyTo) implements Request {
    }

    public record GetBookingsByUser(Integer userId, ActorRef<List<Booking>> replyTo) implements Request {
    }

    public record CreateBooking(Booking booking, ActorRef<ActionPerformed> replyTo) implements Request {
    }

    public record DeleteBookingByUser(Integer userId, ActorRef<ActionPerformed> replyTo) implements Request {
    }

    public record DeleteBookingByShowAndUserId(Integer userId, Integer showId,
                                               ActorRef<ActionPerformed> replyTo) implements Request {
    }

    public record DeleteAllBookings(ActorRef<ActionPerformed> replyTo) implements Request {
    }

    public record GetTheatres(ActorRef<List<Theatre>> replyTo) implements Request {
    }

    public record ActionPerformed(String description) implements Request {
    }

    private BookingActor(ActorContext<Request> context) {
        super(context);
        // Load theatres from CSV file
        theatres = loadTheatreFromJson();
        // Load shows from CSV file and create ShowActor for each show
        showActors = loadShowsFromJson();
    }

    private Map<Integer, ActorRef<ShowActor.Request>> loadShowsFromJson() {
        log.info("Loading shows from CSV file");
        Map<Integer, ActorRef<ShowActor.Request>> showActors = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("data/shows.csv"));
            br.readLine(); // skip header
            String line = br.readLine();
            while (line != null) {
                String[] values = line.split(",");
                Integer showId = Integer.parseInt(values[0]);
                Integer theatreId = Integer.parseInt(values[1]);
                String movieName = values[2];
                Integer price = Integer.parseInt(values[3]);
                Integer seatsAvailable = Integer.parseInt(values[4]);

                // Create ShowActor for each show
                showActors.put(showId,
                        getContext().spawn(
                                ShowActor.create(
                                        new Show(showId, theatres.get(theatreId), movieName, price, seatsAvailable)
                                ),
                                "ShowActor-" + showId));
                line = br.readLine();
            }
        } catch (IOException e) {
            log.info("Error loading shows from CSV file Message: " + e.getMessage());
        }
        return showActors;
    }

    /**
     * Load theatres from CSV file
     *
     * @return Map of theatreId -> theatres
     */
    private Map<Integer, Theatre> loadTheatreFromJson() {
        log.info("Loading theatres from CSV file");
        Map<Integer, Theatre> theatres = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("data/theatres.csv"));
            br.readLine();
            String line = br.readLine();
            while (line != null) {
                String[] values = line.split(",");
                Integer theatreId = Integer.parseInt(values[0]);
                String name = values[1];
                String location = values[2];
                Theatre theatre = new Theatre(theatreId, name, location);
                theatres.put(theatreId, theatre);
                line = br.readLine();
            }
        } catch (IOException e) {
            log.info("Error loading theatres from CSV file. Message: " + e.getMessage());
        }
        return theatres;
    }

    /**
     * Create a new instance of the actor
     *
     * @return Behavior of the actor
     */
    public static Behavior<Request> create() {
        return Behaviors.setup(BookingActor::new);
    }

    @Override
    public Receive<Request> createReceive() {
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
    private Behavior<Request> onDeleteAllBookings(DeleteAllBookings deleteAllBookings) {
        return null;
    }

    private Behavior<Request> onDeleteBookingByShowAndUserId(DeleteBookingByShowAndUserId deleteBookingByShowAndUserId) {
        return null;
    }

    private Behavior<Request> onDeleteBookingByUser(DeleteBookingByUser deleteBookingByUser) {
        return null;
    }

    private Behavior<Request> onCreateBooking(CreateBooking createBooking) {
        return null;
    }

    private Behavior<Request> onGetBookingsByUser(GetBookingsByUser getBookingsByUser) {
        return null;
    }

    private Behavior<Request> onGetShowById(GetShowById request) {
        if (!showActors.containsKey(request.showId())) {
            log.info("Passing request to get show by id");
            request.replyTo.tell(new NotFoundMessage("Show not found"));
        } else {
            showActors.get(request.showId()).tell(new ShowActor.GetShow(request.replyTo));
        }
        return this;
    }

    private Behavior<Request> onGetShowsByTheatreId(GetShowsByTheatreId getShowsByTheatreId) {
        return null;
    }

    /**
     * Returns all the theatres to the sender
     *
     * @param request GetTheatres object containing the replyTo actor reference
     * @return Behavior of the actor
     */
    private Behavior<Request> onGetTheatres(GetTheatres request) {
        log.info("Returning " + theatres.size() + " theatres to the sender");
        request.replyTo.tell(theatres.values().stream().toList());
        return this;
    }

}