package com.hostelfood.controller;

import com.hostelfood.dto.qr.MealTokenResponseDTO;
import com.hostelfood.service.MealTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/meals/{mealId}/token")
@RequiredArgsConstructor
public class StudentMealTokenController {

    private final MealTokenService mealTokenService;

    @PostMapping
    public ResponseEntity<MealTokenResponseDTO> generateToken(
            @PathVariable Long mealId,
            Authentication authentication) {
        MealTokenResponseDTO response = mealTokenService.generateOrGetToken(mealId, authentication.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<MealTokenResponseDTO> getToken(
            @PathVariable Long mealId,
            Authentication authentication) {
        MealTokenResponseDTO response = mealTokenService.getStudentMealToken(mealId, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
