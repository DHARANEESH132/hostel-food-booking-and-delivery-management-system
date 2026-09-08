package com.hostelfood.exception;

public class NoVoteFoundForTokenException extends RuntimeException {
    public NoVoteFoundForTokenException(String message) {
        super(message);
    }
}
