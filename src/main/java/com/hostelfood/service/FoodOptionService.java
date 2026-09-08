package com.hostelfood.service;

import com.hostelfood.dto.food.FoodOptionRequestDTO;
import com.hostelfood.dto.food.FoodOptionResponseDTO;

import java.util.List;

public interface FoodOptionService {

    FoodOptionResponseDTO addFoodOptionToMeal(Long mealId, FoodOptionRequestDTO request);

    List<FoodOptionResponseDTO> getFoodOptionsForMeal(Long mealId);

    FoodOptionResponseDTO updateFoodOption(Long id, FoodOptionRequestDTO request);

    void deleteFoodOption(Long id);
}
