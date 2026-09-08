package com.hostelfood.dto.dashboard;

import com.hostelfood.dto.meal.MealResponseDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardDTO {

    private long totalStudents;
    private long totalMealsScheduled;
    private long todayMealsCount;
    private long totalVotesCast;
    private long totalMealsDelivered;
    private long pendingDeliveriesCount;
    private List<MealResponseDTO> activeVotingMeals;
    private List<MealResponseDTO> activeDeliveryMeals;
}
