package com.hostelfood.service;

import com.hostelfood.dto.common.PagedResponseDTO;
import com.hostelfood.dto.feedback.FeedbackRequestDTO;
import com.hostelfood.dto.feedback.FeedbackResponseDTO;
import com.hostelfood.dto.feedback.MealFeedbackSummaryDTO;
import org.springframework.data.domain.Pageable;

public interface FeedbackService {

    FeedbackResponseDTO submitFeedback(Long mealId, FeedbackRequestDTO request, String userEmail);

    FeedbackResponseDTO getStudentFeedback(Long mealId, String userEmail);

    MealFeedbackSummaryDTO getMealFeedbackSummary(Long mealId, Pageable pageable);

    PagedResponseDTO<FeedbackResponseDTO> getAllFeedbacks(Pageable pageable);
}
