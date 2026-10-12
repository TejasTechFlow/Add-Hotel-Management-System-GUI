package com.hotel.exception;

/** Thrown when business-rule validation fails (e.g. room already booked). */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) { super(message); }
}
