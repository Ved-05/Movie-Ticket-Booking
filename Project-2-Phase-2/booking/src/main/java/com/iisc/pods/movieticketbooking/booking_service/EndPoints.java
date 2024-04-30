package com.iisc.pods.movieticketbooking.booking_service;

import java.lang.System;

public final class EndPoints {
    private static final String BASE_URL_LOCALDEV = "http://localhost";
    private static final String BASE_URL_DOCKER = "http://host.docker.internal";

    private static final String USER_PORT = ":8080";
    private static final String BOOKING_PORT = ":8081";
    private static final String WALLET_PORT = ":8082";

    public static String getUserPort() {
        String retval;
        String dockerString = System.getenv("DOCKER_RUNNING");
        boolean dockerRunning = (dockerString != null && System.getenv("DOCKER_RUNNING").equals("TRUE"));
        if (dockerRunning) {
            retval = BASE_URL_LOCALDEV + USER_PORT;
        } else {
            retval = BASE_URL_LOCALDEV + USER_PORT;
        }
        return retval;
    }

    public static String getBookingPort() {
        String retval;
        String dockerString = System.getenv("DOCKER_RUNNING");
        boolean dockerRunning = (dockerString != null && System.getenv("DOCKER_RUNNING").equals("TRUE"));
        if (dockerRunning) {
            retval = BASE_URL_LOCALDEV + BOOKING_PORT;
        } else {
            retval = BASE_URL_LOCALDEV + BOOKING_PORT;
        }
        return retval;
    }

    public static String getWalletPort() {
        String retval;
        String dockerString = System.getenv("DOCKER_RUNNING");
        boolean dockerRunning = (dockerString != null && System.getenv("DOCKER_RUNNING").equals("TRUE"));
        if (dockerRunning) {
            retval = BASE_URL_LOCALDEV + WALLET_PORT;
        } else {
            retval = BASE_URL_LOCALDEV + WALLET_PORT;
        }
        return retval;
    }
}