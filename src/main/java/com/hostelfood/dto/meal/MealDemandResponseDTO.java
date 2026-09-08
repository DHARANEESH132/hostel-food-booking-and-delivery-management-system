package com.hostelfood.dto.meal;

import com.hostelfood.dto.food.FoodOptionDemandDTO;
import com.hostelfood.enums.MealStatus;
import com.hostelfood.enums.MealType;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealDemandResponseDTO {

    private Long mealId;
    private LocalDate date;
    private MealType mealType;
    private MealStatus status;
    private long totalVotes;
    private List<FoodOptionDemandDTO> optionDemands;
}
