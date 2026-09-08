package com.hostelfood.dto.qr;

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
public class MealTokenResponseDTO {

    private Long id;
    private String tokenCode;
    private String qrCodeBase64;
    private Long mealId;
    private LocalDate mealDate;
    private MealType mealType;
    private Long foodOptionId;
    private String foodName;
    private Long studentId;
    private String studentName;
    private String studentRegistrationNumber;
    private String hostel;
    private String roomNumber;
    private TokenStatus status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
