package com.hostelfood.controller;

import com.hostelfood.dto.food.FoodOptionRequestDTO;
import com.hostelfood.dto.food.FoodOptionResponseDTO;
import com.hostelfood.dto.meal.MealRequestDTO;
import com.hostelfood.dto.meal.MealResponseDTO;
import com.hostelfood.service.FoodOptionService;
import com.hostelfood.service.MealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminMealController {

    private final MealService mealService;
    private final FoodOptionService foodOptionService;

    // --- Meals APIs ---

    @PostMapping("/meals")
    public ResponseEntity<MealResponseDTO> createMeal(@Valid @RequestBody MealRequestDTO request) {
        MealResponseDTO response = mealService.createMeal(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/meals")
    public ResponseEntity<List<MealResponseDTO>> getAllMeals() {
        return ResponseEntity.ok(mealService.getAllMeals());
    }

    @GetMapping("/meals/{id}")
    public ResponseEntity<MealResponseDTO> getMealById(@PathVariable Long id) {
        return ResponseEntity.ok(mealService.getMealById(id));
    }

    @PutMapping("/meals/{id}")
    public ResponseEntity<MealResponseDTO> updateMeal(
            @PathVariable Long id,
            @Valid @RequestBody MealRequestDTO request) {
        return ResponseEntity.ok(mealService.updateMeal(id, request));
    }

    @DeleteMapping("/meals/{id}")
    public ResponseEntity<Void> deleteMeal(@PathVariable Long id) {
        mealService.deleteMeal(id);
        return ResponseEntity.noContent().build();
    }

    // --- Food Options APIs ---

    @PostMapping("/meals/{mealId}/food-options")
    public ResponseEntity<FoodOptionResponseDTO> addFoodOption(
            @PathVariable Long mealId,
            @Valid @RequestBody FoodOptionRequestDTO request) {
        FoodOptionResponseDTO response = foodOptionService.addFoodOptionToMeal(mealId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/meals/{mealId}/food-options")
    public ResponseEntity<List<FoodOptionResponseDTO>> getFoodOptionsForMeal(@PathVariable Long mealId) {
        return ResponseEntity.ok(foodOptionService.getFoodOptionsForMeal(mealId));
    }

    @PutMapping("/food-options/{id}")
    public ResponseEntity<FoodOptionResponseDTO> updateFoodOption(
            @PathVariable Long id,
            @Valid @RequestBody FoodOptionRequestDTO request) {
        return ResponseEntity.ok(foodOptionService.updateFoodOption(id, request));
    }

    @DeleteMapping("/food-options/{id}")
    public ResponseEntity<Void> deleteFoodOption(@PathVariable Long id) {
        foodOptionService.deleteFoodOption(id);
        return ResponseEntity.noContent().build();
    }
}
