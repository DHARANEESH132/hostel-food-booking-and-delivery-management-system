package com.hostelfood.dto.meal;

import com.hostelfood.enums.MealStatus;
import com.hostelfood.enums.MealType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealRequestDTO {

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Meal type is required")
    private MealType mealType;

    @NotNull(message = "Voting start time is required")
    private LocalDateTime votingStartTime;

    @NotNull(message = "Voting end time is required")
    private LocalDateTime votingEndTime;

    @NotNull(message = "Delivery start time is required")
    private LocalDateTime deliveryStartTime;

    @NotNull(message = "Delivery end time is required")
    private LocalDateTime deliveryEndTime;

    private MealStatus status;
}
