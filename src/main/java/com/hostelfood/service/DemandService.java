package com.hostelfood.service;

import com.hostelfood.dto.meal.MealDemandResponseDTO;

public interface DemandService {

    MealDemandResponseDTO getMealDemand(Long mealId);
}
