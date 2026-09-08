package com.hostelfood.dto.vote;

import com.hostelfood.enums.MealType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoteResponseDTO {

    private Long id;
    private Long mealId;
    private LocalDate mealDate;
    private MealType mealType;
    private Long foodOptionId;
    private String foodName;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
