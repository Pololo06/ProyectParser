package com.proyectparser.server.exception;

/** The uploaded source tree is empty, malformed or exceeds the API limits. */
public class InvalidSourceException extends RuntimeException {

    public InvalidSourceException(String message) {
        super(message);
    }

    public InvalidSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
