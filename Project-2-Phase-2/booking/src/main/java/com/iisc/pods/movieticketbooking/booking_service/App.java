package com.iisc.pods.movieticketbooking.booking_service;

import akka.NotUsed;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.server.Route;
import com.iisc.pods.movieticketbooking.booking_service.actors.BookingActor;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletionStage;

/**
 * Main class for the application.
 */
public class App {
    static void startHttpServer(Route route, ActorSystem<?> system) {
        CompletionStage<ServerBinding> futureBinding =
                Http.get(system).newServerAt("0.0.0.0", 8080).bind(route);

        futureBinding.whenComplete((binding, exception) -> {
            if (binding != null) {
                InetSocketAddress address = binding.localAddress();
                system.log().info("Server online at http://{}:{}/",
                        address.getHostString(),
                        address.getPort());
            } else {
                system.log().error("Failed to bind HTTP endpoint, terminating system", exception);
                system.terminate();
            }
        });
    }
    // #start-http-server

    public static void main(String[] args) {
        //#server-bootstrapping
        Behavior<NotUsed> rootBehavior = Behaviors.setup(context -> {
            ActorRef<BookingActor.Request> bookingActor =
                    context.spawn(BookingActor.create(), "BookingActor");

            BookingRoutes bookingRoutes = new BookingRoutes(context.getSystem(), bookingActor);
            startHttpServer(bookingRoutes.bookingServiceRoute(), context.getSystem());

            return Behaviors.empty();
        });

        // boot up server using the route as defined below
        ActorSystem.create(rootBehavior, "MovieTicketBookingHttpServer");
        //#server-bootstrapping
    }
}
