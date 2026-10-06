package com.hostelfood.exception;

public class PublicRegistrationDisabledException extends RuntimeException {
    public PublicRegistrationDisabledException(String message) {
        super(message);
    }
}
