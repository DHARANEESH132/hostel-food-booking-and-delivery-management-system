package com.hostelfood.exception;

public class FoodOptionNotFoundException extends RuntimeException {
    public FoodOptionNotFoundException(String message) {
        super(message);
    }
}
