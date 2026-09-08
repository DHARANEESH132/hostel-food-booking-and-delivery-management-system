package com.hostelfood.dto.report;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionConsumptionDTO {

    private Long foodOptionId;
    private String foodName;
    private long votedCount;
    private long deliveredCount;
    private long unclaimedCount;
    private double wastePercentage;
}
