package com.hostelfood.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "EmailAlreadyExistsException", ex.getMessage());
    }

    @ExceptionHandler(StudentIdAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleStudentIdAlreadyExists(StudentIdAlreadyExistsException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "StudentIdAlreadyExistsException", ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "InvalidCredentialsException", ex.getMessage());
    }

    @ExceptionHandler(MealNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleMealNotFound(MealNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "MealNotFoundException", ex.getMessage());
    }

    @ExceptionHandler(FoodOptionNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleFoodOptionNotFound(FoodOptionNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "FoodOptionNotFoundException", ex.getMessage());
    }

    @ExceptionHandler(VotingClosedException.class)
    public ResponseEntity<Map<String, Object>> handleVotingClosed(VotingClosedException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "VotingClosedException", ex.getMessage());
    }

    @ExceptionHandler(DuplicateVoteException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateVote(DuplicateVoteException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "DuplicateVoteException", ex.getMessage());
    }

    @ExceptionHandler(InvalidFoodOptionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidFoodOption(InvalidFoodOptionException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "InvalidFoodOptionException", ex.getMessage());
    }

    @ExceptionHandler(VoteNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleVoteNotFound(VoteNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "VoteNotFoundException", ex.getMessage());
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleTokenNotFound(TokenNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "TokenNotFoundException", ex.getMessage());
    }

    @ExceptionHandler(TokenAlreadyUsedException.class)
    public ResponseEntity<Map<String, Object>> handleTokenAlreadyUsed(TokenAlreadyUsedException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "TokenAlreadyUsedException", ex.getMessage());
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<Map<String, Object>> handleTokenExpired(TokenExpiredException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "TokenExpiredException", ex.getMessage());
    }

    @ExceptionHandler(NoVoteFoundForTokenException.class)
    public ResponseEntity<Map<String, Object>> handleNoVoteFoundForToken(NoVoteFoundForTokenException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "NoVoteFoundForTokenException", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "ValidationException", message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getClass().getSimpleName(), ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}
