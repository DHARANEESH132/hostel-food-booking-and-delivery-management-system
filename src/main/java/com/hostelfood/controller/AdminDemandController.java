package com.hostelfood.controller;

import com.hostelfood.dto.meal.MealDemandResponseDTO;
import com.hostelfood.service.DemandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/meals/{mealId}/demand")
@RequiredArgsConstructor
public class AdminDemandController {

    private final DemandService demandService;

    @GetMapping
    public ResponseEntity<MealDemandResponseDTO> getMealDemand(@PathVariable Long mealId) {
        return ResponseEntity.ok(demandService.getMealDemand(mealId));
    }
}
