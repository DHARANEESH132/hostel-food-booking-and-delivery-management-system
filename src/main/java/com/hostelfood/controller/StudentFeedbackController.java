package com.hostelfood.controller;

import com.hostelfood.dto.feedback.FeedbackRequestDTO;
import com.hostelfood.dto.feedback.FeedbackResponseDTO;
import com.hostelfood.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/meals/{mealId}/feedback")
@RequiredArgsConstructor
public class StudentFeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<FeedbackResponseDTO> submitFeedback(
            @PathVariable Long mealId,
            @Valid @RequestBody FeedbackRequestDTO request,
            Authentication authentication) {
        FeedbackResponseDTO response = feedbackService.submitFeedback(mealId, request, authentication.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<FeedbackResponseDTO> getStudentFeedback(
            @PathVariable Long mealId,
            Authentication authentication) {
        FeedbackResponseDTO response = feedbackService.getStudentFeedback(mealId, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
