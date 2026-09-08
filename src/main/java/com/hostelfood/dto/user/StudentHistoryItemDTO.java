package com.hostelfood.dto.user;

import com.hostelfood.enums.MealType;
import com.hostelfood.enums.TokenStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentHistoryItemDTO {

    private Long mealId;
    private LocalDate mealDate;
    private MealType mealType;
    private Long foodOptionId;
    private String foodName;
    private LocalDateTime votedAt;
    private TokenStatus tokenStatus;
    private String tokenCode;
    private boolean delivered;
    private LocalDateTime deliveredAt;
}
