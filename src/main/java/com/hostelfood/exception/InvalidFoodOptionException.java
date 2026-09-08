package com.hostelfood.exception;

public class InvalidFoodOptionException extends RuntimeException {
    public InvalidFoodOptionException(String message) {
        super(message);
    }
}
