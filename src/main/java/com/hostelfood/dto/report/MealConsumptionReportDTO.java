package com.hostelfood.dto.report;

import com.hostelfood.enums.MealType;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealConsumptionReportDTO {

    private Long mealId;
    private LocalDate mealDate;
    private MealType mealType;
    private long totalVoted;
    private long totalDelivered;
    private long unclaimedMeals;
    private double consumptionRatePercentage;
    private List<OptionConsumptionDTO> optionBreakdowns;
}
