package com.hostelfood.dto.delivery;

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
public class QrValidationResponseDTO {

    private String tokenCode;
    private boolean valid;
    private TokenStatus status;
    private Long studentId;
    private String studentName;
    private String studentRegistrationNumber;
    private String hostel;
    private String roomNumber;
    private Long mealId;
    private LocalDate mealDate;
    private MealType mealType;
    private Long foodOptionId;
    private String foodName;
    private LocalDateTime deliveryStartTime;
    private LocalDateTime deliveryEndTime;
    private String message;
}
