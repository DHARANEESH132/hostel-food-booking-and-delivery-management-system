package com.hostelfood.dto.delivery;

import com.hostelfood.enums.MealType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryResponseDTO {

    private Long deliveryId;
    private String tokenCode;
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
    private String deliveredByAdmin;
    private LocalDateTime deliveredAt;
    private String notes;
    private String message;
}
