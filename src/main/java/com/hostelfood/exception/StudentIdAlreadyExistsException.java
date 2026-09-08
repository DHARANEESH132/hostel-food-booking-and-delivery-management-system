package com.hostelfood.exception;

public class StudentIdAlreadyExistsException extends RuntimeException {
    public StudentIdAlreadyExistsException(String message) {
        super(message);
    }
}
