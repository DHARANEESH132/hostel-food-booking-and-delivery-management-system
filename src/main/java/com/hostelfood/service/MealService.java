package com.hostelfood.service;

import com.hostelfood.dto.meal.MealRequestDTO;
import com.hostelfood.dto.meal.MealResponseDTO;

import java.util.List;

public interface MealService {

    MealResponseDTO createMeal(MealRequestDTO request);

    List<MealResponseDTO> getAllMeals();

    MealResponseDTO getMealById(Long id);

    MealResponseDTO updateMeal(Long id, MealRequestDTO request);

    void deleteMeal(Long id);

    List<MealResponseDTO> getMealsForStudents();
}
