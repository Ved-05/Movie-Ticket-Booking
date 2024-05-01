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
import com.iisc.pods.movieticketbooking.booking_service.actors.ShowActor;
import com.iisc.pods.movieticketbooking.booking_service.model.Show;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

import static com.iisc.pods.movieticketbooking.booking_service.actors.BookingWorker.BOOKING_WORKER_SERVICE_KEY;

/**
 * Main class for the application.
 */
public class App {
    private final static Logger log = Logger.getLogger(App.class.getName());

    private static class RootBehavior {
        static Behavior<Void> create(Map<Integer, Show> integerShowMap) {
            return Behaviors.setup(context -> {
                Cluster cluster = Cluster.get(context.getSystem());
                Set<Integer> showIds = integerShowMap.keySet();
                for (int i=0; i < 50; i++) {
                    ActorRef<BookingWorker.Request> worker = context.spawn(BookingWorker.create(showIds), "BookingWorker" + i);
                    context.getSystem().receptionist().tell(Receptionist.register(BOOKING_WORKER_SERVICE_KEY, worker));
                }
                GroupRouter<BookingWorker.Request> group = Routers.group(BOOKING_WORKER_SERVICE_KEY);
                ActorRef<BookingWorker.Request> router = context.spawn(group, "worker-group");

                // If the node is primary, initialize the show actors and start the HTTP server
                if (cluster.selfMember().hasRole("primary")) {
                    integerShowMap.values().forEach(show -> ShowActor.initSharding(context.getSystem(), show));
                    // Create a map of theatre id -> show id from integerShowMap
                    Map<Integer, Set<Integer>> theatreShowMap = new HashMap<>();
                    integerShowMap.forEach((showId, show) -> {
                        Set<Integer> showIdsForTheatre = theatreShowMap.getOrDefault(show.theatre_id(), new HashSet<>());
                        showIdsForTheatre.add(showId);
                        theatreShowMap.put(show.theatre_id(), showIdsForTheatre);
                    });
                    ActorRef<BookingActor.Request> bookingActor =
                            context.spawn(BookingActor.create(router, theatreShowMap), "BookingActor");
                    // Start the HTTP server
                    BookingRoutes bookingRoutes = new BookingRoutes(context.getSystem(), bookingActor);
                    startHttpServer(bookingRoutes.bookingServiceRoute(), context.getSystem());
                }
                // If the node is secondary, join the primary node
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

        ActorSystem<Void> system = ActorSystem.create(RootBehavior.create(readShowsFromCSV()), "MovieTicketBookingClusterSystem", config);
    }

    /**
     * Initialize the show actors.
     */
    private static Map<Integer, Show> readShowsFromCSV() {
        log.info("Initializing show actors");
        Map<Integer, Show> shows = new HashMap<>();
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
                shows.put(showId, new Show(showId, theatreId, movieName, price, seatsAvailable));
                line = br.readLine();
            }
        } catch (IOException e) {
            log.info("Error loading shows from CSV file Message: " + e.getMessage());
        }
        return shows;
    }
}
