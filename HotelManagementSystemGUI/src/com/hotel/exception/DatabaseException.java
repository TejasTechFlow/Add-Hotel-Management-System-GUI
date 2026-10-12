package com.hotel.exception;

/** Thrown when a database operation fails at the DAO layer. */
public class DatabaseException extends RuntimeException {
    public DatabaseException(String message)              { super(message); }
    public DatabaseException(String message, Throwable cause) { super(message, cause); }
}
