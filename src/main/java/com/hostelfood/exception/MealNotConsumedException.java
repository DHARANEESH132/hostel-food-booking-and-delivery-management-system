package com.hostelfood.exception;

public class MealNotConsumedException extends RuntimeException {
    public MealNotConsumedException(String message) {
        super(message);
    }
}
