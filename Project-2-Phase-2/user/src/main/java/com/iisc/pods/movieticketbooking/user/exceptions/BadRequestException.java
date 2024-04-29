package com.iisc.pods.movieticketbooking.user.exceptions;

/**
 * Exception thrown when a request is made with invalid parameters.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}