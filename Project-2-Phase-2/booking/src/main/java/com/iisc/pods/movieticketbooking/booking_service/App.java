package com.iisc.pods.movieticketbooking.booking_service;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.GroupRouter;
import akka.actor.typed.javadsl.Routers;
import akka.actor.typed.receptionist.Receptionist;
import akka.cluster.typed.Cluster;
import akka.cluster.typed.Join;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.server.Route;
import com.iisc.pods.movieticketbooking.booking_service.actors.BookingActor;
import com.iisc.pods.movieticketbooking.booking_service.actors.BookingWorker;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

import static com.iisc.pods.movieticketbooking.booking_service.actors.BookingWorker.BOOKING_WORKER_SERVICE_KEY;

/**
 * Main class for the application.
 */
public class App {

    private static class RootBehavior {
        static Behavior<Void> create() {
            return Behaviors.setup(context -> {
                Cluster cluster = Cluster.get(context.getSystem());
                for (int i=0; i < 50; i++) {
                    ActorRef<BookingWorker.Request> worker = context.spawn(BookingWorker.create(), "BookingWorker" + i);
                    context.getSystem().receptionist().tell(Receptionist.register(BOOKING_WORKER_SERVICE_KEY, worker));
                }
                GroupRouter<BookingWorker.Request> group = Routers.group(BOOKING_WORKER_SERVICE_KEY);
                ActorRef<BookingWorker.Request> router = context.spawn(group, "worker-group");

                if (cluster.selfMember().hasRole("primary")) {
                    ActorRef<BookingActor.Request> bookingActor =
                            context.spawn(BookingActor.create(router), "BookingActor");
                    BookingRoutes bookingRoutes = new BookingRoutes(context.getSystem(), bookingActor);
                    startHttpServer(bookingRoutes.bookingServiceRoute(), context.getSystem());
                }
                if (cluster.selfMember().hasRole("secondary")) {
                    cluster.manager().tell(Join.create(cluster.selfMember().address()));
                }

                return Behaviors.empty();
            });
        }
    }

    /**
     * Start the HTTP server.
     * Will be invoked for the primary node creation
     *
     * @param route Route to be used for the server
     * @param system Actor system
     */
    static void startHttpServer(Route route, ActorSystem<?> system) {
        String dockerString = System.getenv("DOCKER_RUNNING");
        boolean dockerRunning = (dockerString != null && System.getenv("DOCKER_RUNNING").equals("TRUE"));
        CompletionStage<ServerBinding> futureBinding =
                Http.get(system).newServerAt("0.0.0.0", (8081)).bind(route);

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

    /**
     * Main method for the application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: App <port>");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        String role = port == 8083 ? "primary" : "secondary";
        startup(role, port);
    }

    private static void startup(String role, int port) {
        // Override the configuration of the port
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("akka.remote.artery.canonical.port", port);
        overrides.put("akka.cluster.roles", Collections.singletonList(role));

        Config config = ConfigFactory.parseMap(overrides).withFallback(ConfigFactory.load());

        // Create an Akka system
        ActorSystem<Void> system = ActorSystem.create(RootBehavior.create(), "MovieTicketBookingClusterSystem", config);
    }
}
