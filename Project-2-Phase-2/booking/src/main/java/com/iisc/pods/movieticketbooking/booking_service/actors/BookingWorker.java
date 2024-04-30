package com.iisc.pods.movieticketbooking.booking_service.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.actor.typed.receptionist.ServiceKey;
import com.iisc.pods.movieticketbooking.booking_service.BookingRoutes;
import com.iisc.pods.movieticketbooking.booking_service.model.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

public class BookingWorker extends AbstractBehavior<BookingWorker.Request> {
    private final static Logger log = Logger.getLogger(BookingRoutes.class.getName());

    public static final ServiceKey<BookingWorker.Request> BOOKING_WORKER_SERVICE_KEY =
            ServiceKey.create(BookingWorker.Request.class, "BookingWorker");

    private final Map<Integer, Theatre> theatres;
    private final Map<Integer, ActorRef<ShowActor.Request>> showActors;
    private final Map<Integer, Set<Integer>> theatreIdToShowId;

    public sealed interface Request {
    }

    public record GetShowsByTheatreId(Integer theatreId, ActorRef<List<Show>> replyTo) implements Request {
    }

    public record GetShowById(Integer showId, ActorRef<ActorModel> replyTo) implements Request {
    }

    public record GetBookingsByUser(Integer userId, ActorRef<List<Booking>> replyTo) implements Request {
    }

    public record CreateBooking(Booking booking, ActorRef<BookingActor.Request> replyTo) implements Request {
    }

    public record DeleteBookingByUser(Integer userId, ActorRef<BookingActor.ActionResponse> replyTo) implements Request {
    }

    public record DeleteBookingByShowAndUserId(Integer userId, Integer showId,
                                               ActorRef<BookingActor.ActionResponse> replyTo) implements Request {
    }

    public record DeleteAllBookings(ActorRef<BookingActor.ActionResponse> replyTo) implements Request {
    }

    public record GetTheatres(ActorRef<List<Theatre>> replyTo) implements Request {
    }

    private BookingWorker(ActorContext<Request> context) {
        super(context);
        this.theatreIdToShowId = new HashMap<>();
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
                                        new Show(showId, theatreId, movieName, price, seatsAvailable)
                                ),
                                "ShowActor-" + showId));
                this.theatreIdToShowId.get(theatreId).add(showId);
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
                this.theatreIdToShowId.put(theatreId, new HashSet<>());
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
        return Behaviors.setup(BookingWorker::new);
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

    /**
     * Handles the delete all bookings request
     *
     * @param request DeleteAllBookings record
     * @return Behavior of the actor
     */
    private Behavior<Request> onDeleteAllBookings(DeleteAllBookings request) {
        log.info("Forwarding delete all bookings request to the show actors");
        this.showActors.values().forEach(showActor -> showActor.tell(new ShowActor.DeleteAllBookings(request.replyTo)));
        request.replyTo.tell(new BookingActor.ActionPerformed("All bookings deleted."));
        return this;
    }

    private Behavior<Request> onDeleteBookingByShowAndUserId(DeleteBookingByShowAndUserId request) {
        if (!this.showActors.containsKey(request.showId)) {
            request.replyTo.tell(new BookingActor.ActionFailed("Failed. Show not exists."));
            return this;
        }
        log.info("Forwarding delete by user id request to the show actor : " + request.showId);
        this.showActors.get(request.showId).tell(new ShowActor.DeleteBookingForUser(request.userId, request.replyTo));
        return this;
    }

    /**
     * Handles the delete booking by user request.
     *
     * @param request DeleteBookingByUser record
     * @return Behavior of the actor
     */
    private Behavior<Request> onDeleteBookingByUser(DeleteBookingByUser request) {
        log.info("Forwarding delete by user id request to the show actors");
        // Delete bookings from all the show actors for user id. Respond if all the bookings are deleted.
        boolean isActionFailed = this.showActors.values().stream()
                .map(showActor -> {
                    CompletionStage<BookingActor.ActionResponse> completionStage = AskPattern.ask(showActor,
                            ref -> new ShowActor.DeleteBookingForUser(request.userId, ref),
                            Duration.ofSeconds(5), getContext().getSystem().scheduler());
                    return completionStage.toCompletableFuture();
                }).map(CompletableFuture::join)
                .allMatch(actionResponse -> actionResponse instanceof BookingActor.ActionFailed);

        BookingActor.ActionResponse actionStatus = isActionFailed ? new BookingActor.ActionFailed("Failed. Some bookings could not be deleted.") :
                new BookingActor.ActionPerformed("All bookings deleted for user id: " + request.userId);
        request.replyTo.tell(actionStatus);
        return this;
    }

    /**
     * Handles the create booking request. If the show does not exist, it will return the show not found to the sender.
     *
     * @param request CreateBooking object containing the booking details and replyTo actor reference
     * @return Behavior of the actor
     */
    private Behavior<Request> onCreateBooking(CreateBooking request) {
        if (!this.showActors.containsKey(request.booking.show_id())) {
            request.replyTo.tell(new BookingActor.ActionFailed("Failed. Show not exists."));
            return this;
        }
        log.info("Forwarding request to create booking to the show actor");
        this.showActors.get(request.booking.show_id()).tell(new ShowActor.CreateBooking(request.booking, request.replyTo));
        return this;
    }

    /**
     * Returns bookings by user to the sender if bookings are found else returns an empty list
     *
     * @param request GetBookingsByUser object containing the user id and replyTo actor reference
     * @return Behavior of the actor
     */
    private Behavior<Request> onGetBookingsByUser(GetBookingsByUser request) {
        log.info("Forwarding user id to the show actors");
        // Get booking details from the show actors and then combine the results to a list
        List<Booking> bookingsForUser = this.showActors.values().stream()
                .map(showActor -> {
                    CompletionStage<List<Booking>> completionStage = AskPattern.ask(showActor,
                            ref -> new ShowActor.GetBookingForUser(request.userId, ref),
                            Duration.ofSeconds(5), getContext().getSystem().scheduler());
                    return completionStage.toCompletableFuture();
                }).map(CompletableFuture::join)
                .reduce(new ArrayList<>(), (acc, bookings) -> {
                    acc.addAll(bookings);
                    return acc;
                }, (acc1, acc2) -> {
                    acc1.addAll(acc2);
                    return acc1;
                });
        request.replyTo.tell(bookingsForUser);
        return this;
    }

    /**
     * Returns the show not found to sender if show is not found else forwards the request to the show actor
     *
     * @param request GetShowById object containing the show id and replyTo actor reference
     * @return Behavior of the actor
     */
    private Behavior<Request> onGetShowById(GetShowById request) {
        if (!showActors.containsKey(request.showId())) {
            log.info("Forwarding request to get show by id");
            request.replyTo.tell(new NotFoundMessage("Show not found"));
        } else {
            showActors.get(request.showId()).tell(new ShowActor.GetShow(request.replyTo));
        }
        return this;
    }

    /**
     * Returns the shows by theatre id to the sender
     *
     * @param request GetShowsByTheatreId object containing the theatre id and replyTo actor reference
     * @return Behavior of the actor
     */
    private Behavior<Request> onGetShowsByTheatreId(GetShowsByTheatreId request) {
        if (!this.theatreIdToShowId.containsKey(request.theatreId)) {
            request.replyTo.tell(Collections.emptyList());
            return this;
        }
        log.info("Forwarding shows by theatre id to the show actors");
        // Get show details for all shows from the show actors and then combine the results to a list
        List<Show> shows = this.theatreIdToShowId.get(request.theatreId).stream()
                .map(showId -> {
                    log.info("Getting show by id: " + showId);
                    CompletionStage<ActorModel> completionStage = AskPattern.ask(showActors.get(showId),
                            ShowActor.GetShow::new, Duration.ofSeconds(5), getContext().getSystem().scheduler());
                    return completionStage.toCompletableFuture();
                }).map(CompletableFuture::join)
                .map(show -> (Show) show)
                .reduce(new ArrayList<>(), (acc, show) -> {
                    acc.add(show);
                    return acc;
                }, (acc1, acc2) -> {
                    acc1.addAll(acc2);
                    return acc1;
                });
        request.replyTo.tell(shows);
        return this;
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