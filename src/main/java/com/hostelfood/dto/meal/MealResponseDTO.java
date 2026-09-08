package com.hostelfood.dto.meal;

import com.hostelfood.dto.food.FoodOptionResponseDTO;
import com.hostelfood.enums.MealStatus;
import com.hostelfood.enums.MealType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealResponseDTO {

    private Long id;
    private LocalDate date;
    private MealType mealType;
    private LocalDateTime votingStartTime;
    private LocalDateTime votingEndTime;
    private LocalDateTime deliveryStartTime;
    private LocalDateTime deliveryEndTime;
    private MealStatus status;

    @Builder.Default
    private List<FoodOptionResponseDTO> foodOptions = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
