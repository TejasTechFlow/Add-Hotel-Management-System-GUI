package com.hotel.exception;

/** Thrown when a requested entity does not exist. */
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) { super(message); }
}
