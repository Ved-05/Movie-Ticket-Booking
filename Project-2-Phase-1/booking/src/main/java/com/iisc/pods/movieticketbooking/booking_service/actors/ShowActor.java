package com.iisc.pods.movieticketbooking.booking_service.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.iisc.pods.movieticketbooking.booking_service.BookingRoutes;
import com.iisc.pods.movieticketbooking.booking_service.model.Booking;
import com.iisc.pods.movieticketbooking.booking_service.model.Show;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ShowActor extends AbstractBehavior<ShowActor.Request> {
    private final static Logger log = Logger.getLogger(BookingRoutes.class.getName());

    public sealed interface Request {
    }

    public record GetShow(ActorRef<Show> replyTo) implements Request {
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
                .build();
    }

    private Behavior<Request> onGetShow(GetShow getShow) {
        log.info("Received request to get show");
        getShow.replyTo.tell(this.show);
        return this;
    }
}