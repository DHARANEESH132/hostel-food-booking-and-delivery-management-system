package com.hostelfood.service;

import com.hostelfood.dto.qr.MealTokenResponseDTO;

public interface MealTokenService {

    MealTokenResponseDTO generateOrGetToken(Long mealId, String studentEmail);

    MealTokenResponseDTO getStudentMealToken(Long mealId, String studentEmail);
}
