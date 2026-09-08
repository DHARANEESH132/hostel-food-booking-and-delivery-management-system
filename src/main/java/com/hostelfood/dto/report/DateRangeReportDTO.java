package com.hostelfood.dto.report;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DateRangeReportDTO {

    private LocalDate startDate;
    private LocalDate endDate;
    private long totalMeals;
    private long totalVotes;
    private long totalDelivered;
    private long totalUnclaimed;
    private double overallConsumptionRatePercentage;
    private List<MealConsumptionReportDTO> mealReports;
}
