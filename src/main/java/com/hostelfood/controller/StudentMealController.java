package com.hostelfood.controller;

import com.hostelfood.dto.meal.MealResponseDTO;
import com.hostelfood.service.MealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentMealController {

    private final MealService mealService;

    @GetMapping("/meals")
    public ResponseEntity<List<MealResponseDTO>> getMeals() {
        return ResponseEntity.ok(mealService.getMealsForStudents());
    }

    @GetMapping("/meals/{id}")
    public ResponseEntity<MealResponseDTO> getMealById(@PathVariable Long id) {
        return ResponseEntity.ok(mealService.getMealById(id));
    }
}
