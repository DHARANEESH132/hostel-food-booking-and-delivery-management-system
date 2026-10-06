package com.hostelfood.controller;

import com.hostelfood.dto.common.PagedResponseDTO;
import com.hostelfood.dto.feedback.FeedbackResponseDTO;
import com.hostelfood.dto.feedback.MealFeedbackSummaryDTO;
import com.hostelfood.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminFeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping("/meals/{mealId}/feedback")
    public ResponseEntity<MealFeedbackSummaryDTO> getMealFeedbackSummary(
            @PathVariable Long mealId,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(feedbackService.getMealFeedbackSummary(mealId, pageable));
    }

    @GetMapping("/feedbacks")
    public ResponseEntity<PagedResponseDTO<FeedbackResponseDTO>> getAllFeedbacks(
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(feedbackService.getAllFeedbacks(pageable));
    }
}
