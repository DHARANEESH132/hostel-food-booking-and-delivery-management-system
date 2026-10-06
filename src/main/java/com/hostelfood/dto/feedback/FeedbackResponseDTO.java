package com.hostelfood.dto.feedback;

import com.hostelfood.enums.FeedbackCategory;
import com.hostelfood.enums.MealType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackResponseDTO {

    private Long id;
    private Long mealId;
    private MealType mealType;
    private LocalDate mealDate;
    private Long foodOptionId;
    private String foodOptionName;
    private Long userId;
    private String studentName;
    private String studentId;
    private Integer rating;
    private FeedbackCategory category;
    private String comment;
    private LocalDateTime createdAt;
}
