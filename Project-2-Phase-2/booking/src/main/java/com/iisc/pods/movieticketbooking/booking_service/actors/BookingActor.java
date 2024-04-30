package com.iisc.pods.movieticketbooking.booking_service.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.cluster.ClusterEvent;
import akka.cluster.typed.Cluster;
import akka.cluster.typed.Subscribe;
import com.iisc.pods.movieticketbooking.booking_service.BookingRoutes;
import com.iisc.pods.movieticketbooking.booking_service.model.ActorModel;
import com.iisc.pods.movieticketbooking.booking_service.model.Booking;
import com.iisc.pods.movieticketbooking.booking_service.model.Show;
import com.iisc.pods.movieticketbooking.booking_service.model.Theatre;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class BookingActor extends AbstractBehavior<BookingActor.Request> {
    private final static Logger log = Logger.getLogger(BookingRoutes.class.getName());

    private final ActorRef<BookingWorker.Request> workers;
    public sealed interface Request {
    }

    public sealed interface ActionResponse extends Request {
    }

    public record GetShowsByTheatreId(Integer theatreId, ActorRef<List<Show>> replyTo) implements Request {
    }

    public record GetShowById(Integer showId, ActorRef<ActorModel> replyTo) implements Request {
    }

    public record GetBookingsByUser(Integer userId, ActorRef<List<Booking>> replyTo) implements Request {
    }

    public record CreateBooking(Booking booking, ActorRef<Request> replyTo) implements Request {
    }

    public record DeleteBookingByUser(Integer userId, ActorRef<ActionResponse> replyTo) implements Request {
    }

    public record DeleteBookingByShowAndUserId(Integer userId, Integer showId,
                                               ActorRef<ActionResponse> replyTo) implements Request {
    }

    public record DeleteAllBookings(ActorRef<ActionResponse> replyTo) implements Request {
    }

    public record GetTheatres(ActorRef<List<Theatre>> replyTo) implements Request {
    }

    public record ActionPerformed(String description) implements ActionResponse {
    }

    public record ActionFailed(String description) implements ActionResponse {
    }

    // internal adapted cluster events only
        private record ReachabilityChange(ClusterEvent.ReachabilityEvent reachabilityEvent) implements Request {
    }

    private record MemberChange(ClusterEvent.MemberEvent memberEvent) implements Request {
    }

    private Behavior<Request> onReachabilityChange(ReachabilityChange reachabilityChange) {
        log.info("Reachability change detected: " + reachabilityChange.reachabilityEvent);
        return this;
    }

    private Behavior<Request> onMemberChange(MemberChange memberChange) {
        log.info("Member change detected: " + memberChange.memberEvent);
        return this;
    }
    private final Map<Integer, Theatre> theatres;
    private final Map<Integer, Set<Integer>> theatreIdToShowId;

    private BookingActor(ActorContext<Request> context, ActorRef<BookingWorker.Request> workers, Map<Integer, Set<Integer>> theatreIdToShowId) {
        super(context);
        Cluster cluster = Cluster.get(context.getSystem());
        ActorRef<ClusterEvent.MemberEvent> member = context.messageAdapter(ClusterEvent.MemberEvent.class, MemberChange::new);
        ActorRef<ClusterEvent.ReachabilityEvent> reachability = context.messageAdapter(ClusterEvent.ReachabilityEvent.class, ReachabilityChange::new);
        cluster.subscriptions().tell(Subscribe.create(member, ClusterEvent.MemberEvent.class)); 
        cluster.subscriptions().tell(Subscribe.create(reachability, ClusterEvent.ReachabilityEvent.class));
        // Worker pool
        this.workers = workers;
        this.theatreIdToShowId = theatreIdToShowId;
        theatres = loadTheatreFromJson();
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
    public static Behavior<Request> create(ActorRef<BookingWorker.Request> router, Map<Integer, Set<Integer>> theatreIdToShowId) {
        return Behaviors.setup(context -> new BookingActor(context, router, theatreIdToShowId));
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
                .onMessage(ReachabilityChange.class, this::onReachabilityChange)
                .onMessage(MemberChange.class, this::onMemberChange)
                .build();
    }

    /**
     * Forwards request to be dealt by worker
     *
     * @param request DeleteAllBookings record
     * @return Behavior of the actor
     */
    private Behavior<Request> onDeleteAllBookings(DeleteAllBookings request) {
        log.info("Forwarding delete all bookings request to the show actors");
        this.workers.tell(new BookingWorker.DeleteAllBookings(request.replyTo));
        return this;
    }

    /**
     * Forwards request to be dealt by worker
     *
     * @param request DeleteBookingByShowAndUserId record
     * @return Behavior of the actor
     */
    private Behavior<Request> onDeleteBookingByShowAndUserId(DeleteBookingByShowAndUserId request) {
        log.info("Forwarding delete by user id request to the show actor : " + request.showId);
        this.workers.tell(new BookingWorker.DeleteBookingByShowAndUserId(request.userId, request.showId, request.replyTo));
        return this;
    }

    /**
     * Forwards request to be dealt by worker
     *
     * @param request DeleteBookingByUser record
     * @return Behavior of the actor
     */
    private Behavior<Request> onDeleteBookingByUser(DeleteBookingByUser request) {
        log.info("Forwarding delete by user id request to the show actors");
        this.workers.tell(new BookingWorker.DeleteBookingByUser(request.userId, request.replyTo));
        return this;
    }

    /**
     * Forwards request to be dealt by worker
     *
     * @param request CreateBooking object containing the booking details and replyTo actor reference
     * @return Behavior of the actor
     */
    private Behavior<Request> onCreateBooking(CreateBooking request) {
        log.info("Forwarding request to create booking to the show actor");
        this.workers.tell(new BookingWorker.CreateBooking(request.booking, request.replyTo));
        return this;
    }

    /**
     * Forwards request to be dealt by worker
     *
     * @param request GetBookingsByUser object containing the user id and replyTo actor reference
     * @return Behavior of the actor
     */
    private Behavior<Request> onGetBookingsByUser(GetBookingsByUser request) {
        log.info("Forwarding user id to the show actors");
        this.workers.tell(new BookingWorker.GetBookingsByUser(request.userId, request.replyTo));
        return this;
    }

    /**
     * Forwards request to be dealt by worker
     *
     * @param request GetShowById object containing the show id and replyTo actor reference
     * @return Behavior of the actor
     */
    private Behavior<Request> onGetShowById(GetShowById request) {
        log.info("Forwarding request to get show by id");
        this.workers.tell(new BookingWorker.GetShowById(request.showId, request.replyTo));
        return this;
    }

    /**
     * Forwards request to be dealt by worker
     *
     * @param request GetShowsByTheatreId object containing the theatre id and replyTo actor reference
     * @return Behavior of the actor
     */
    private Behavior<Request> onGetShowsByTheatreId(GetShowsByTheatreId request) {
        log.info("Forwarding shows by theatre id to the show actors");
        this.workers.tell(new BookingWorker.GetShowsByTheatreId(request.theatreId, this.theatreIdToShowId,
                request.replyTo));
        return this;
    }

    /**
     * Forwards request to be dealt by worker
     *
     * @param request GetTheatres object containing the replyTo actor reference
     * @return Behavior of the actor
     */
    private Behavior<Request> onGetTheatres(GetTheatres request) {
        log.info("Forwarding theatres to the worker");
        this.workers.tell(new BookingWorker.GetTheatres(request.replyTo, theatres));
        return this;
    }

}