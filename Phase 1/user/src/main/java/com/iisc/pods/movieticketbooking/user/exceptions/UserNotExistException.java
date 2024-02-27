package com.iisc.pods.movieticketbooking.user.exceptions;

/**
 * Exception thrown when a user is not found in the system.
 */
public class UserNotExistException extends RuntimeException {
    public UserNotExistException(String message) {
        super(message);
    }
}
