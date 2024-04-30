package com.iisc.pods.movieticketbooking.booking_service;

import akka.NotUsed;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.GroupRouter;
import akka.actor.typed.javadsl.Routers;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.server.Route;
import com.iisc.pods.movieticketbooking.booking_service.actors.BookingActor;
import com.iisc.pods.movieticketbooking.booking_service.model.Booking;

import akka.cluster.typed.Cluster;


import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.concurrent.CompletionStage;

import javax.swing.GroupLayout.Group;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
/**
 * Main class for the application.
 */
public class App {
    /**
     * Start the HTTP server.
     * Will be invoked for the primary node creation
     * 
     * @param route
     * @param system
     */
    static void startHttpServer(Route route, ActorSystem<?> system) {
        String dockerString = System.getenv("DOCKER_RUNNING");
        boolean dockerRunning = (dockerString != null && System.getenv("DOCKER_RUNNING").equals("TRUE"));
        CompletionStage<ServerBinding> futureBinding =
                Http.get(system).newServerAt("0.0.0.0", (dockerRunning ? 8081:8081)).bind(route);

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
     * @param args
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: App <port>");
            System.exit(1);
        }

        if (Integer.parseInt(args[0]) == 8083){
            //# Primary node creation
            startup("mainbooking", 8083);
        }
        else {
            //# Secondary node creation 
            startup("clusternodes", Integer.parseInt(args[0]));
        }
    }

    private static Behavior<Void> rootBehavior() {
        ServiceKey<BookingActor.Request> serviceKey = ServiceKey.create(BookingActor.Request.class, "booking-worker");

        //#server-bootstrapping
        Behavior<Void> rootBehavior = Behaviors.setup(context -> {

            Cluster cluster = Cluster.get(context.getSystem());

            //# Primary node creation request

            if (cluster.selfMember().hasRole("mainbooking")) {
                ActorRef<BookingActor.Request> bookingActor = context.spawn(BookingActor.create(), "BookingActor");
                context.getSystem().receptionist().tell(Receptionist.register(serviceKey, bookingActor));
                GroupRouter<BookingActor.Request> bookingRouter = Routers.group(serviceKey);
                ActorRef<BookingActor.Request> groupRouter = context.spawn(bookingRouter, "bookingRouter");
                
                /**
                 * Requirements: 
                 *  - If the given port number is 8083, i.e., it is the primary node, it must start a HTTP 
                 *    Server, which listens to requests from outside. This http server must bind to port
                 *    8081.  In this mode, the program must spawn the (singleton) Booking actor, which
                 *    would be the gateway actor that the route logic will contact.
                 * 
                 *  - Unlike in Phase 1, the Booking actor should not spawn the worker actors directly 
                 *    (see the definition of worker actor above in the Phase 1 description). As if it
                 *    did so, all work would happen on the primary node and the other secondary nodes in
                 *    the cluster will have no work to do.
                 * 
                 *  - Rather, each node (the primary node and each secondary node) should at startup
                 *    time spawn a fixed number of worker actors of each worker actor type (say 50 actors
                 *    of each type), and contribute them to the cluster using the Group Router mechanism.
                 *    (Recall, the Group Router is intuitively like a cross-cluster pool of actors. Read about
                 *    Group Routers here.) Whatever incoming messages the Booking actor does not
                 *    respond to itself, it should forward to the router, which will internally forward to one of
                 *    the worker actors. This way the total work is distributed across all the worker actors
                 *    in all the nodes.

                 */
                BookingRoutes bookingRoutes = new BookingRoutes(context.getSystem(), groupRouter);
                startHttpServer(bookingRoutes.bookingServiceRoute(), context.getSystem());
            }

            if (cluster.selfMember().hasRole("clusternodes")) {
                /**
                 * Requirements:
                 * The nodes started in secondary mode should not start the HTTP server, and should not spawn the
                 * Booking actor. They should just join the cluster.
                 * 
                 */
                // TODO: Add code to start the cluster nodes
            }

            return Behaviors.empty();
        });
        return rootBehavior;
    }

    private static void startup(String role, int port) {
        // Override the configuration of the port
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("akka.remote.artery.canonical.port", port);
        overrides.put("akka.cluster.roles", Collections.singletonList(role));
        
        Config config = ConfigFactory.parseMap(overrides)
            .withFallback(ConfigFactory.load("bookingservs.conf"));
        
        // Create an Akka system
        ActorSystem<Void> system = ActorSystem.create(rootBehavior(), "MovieTicketBookingClusterSystem", config);
    }
}
