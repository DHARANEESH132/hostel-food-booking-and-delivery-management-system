package com.hostelfood.dto.feedback;

import com.hostelfood.dto.common.PagedResponseDTO;
import com.hostelfood.enums.FeedbackCategory;
import com.hostelfood.enums.MealType;
import lombok.*;

import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealFeedbackSummaryDTO {

    private Long mealId;
    private MealType mealType;
    private LocalDate mealDate;
    private long totalFeedbacks;
    private Double averageRating;
    private Map<Integer, Long> starDistribution;
    private Map<FeedbackCategory, Long> categoryDistribution;
    private PagedResponseDTO<FeedbackResponseDTO> feedbacks;
}
