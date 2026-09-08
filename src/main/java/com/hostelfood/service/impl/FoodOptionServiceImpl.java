package com.hostelfood.service.impl;

import com.hostelfood.dto.food.FoodOptionRequestDTO;
import com.hostelfood.dto.food.FoodOptionResponseDTO;
import com.hostelfood.entity.FoodOption;
import com.hostelfood.entity.Meal;
import com.hostelfood.exception.FoodOptionNotFoundException;
import com.hostelfood.exception.MealNotFoundException;
import com.hostelfood.repository.FoodOptionRepository;
import com.hostelfood.repository.MealRepository;
import com.hostelfood.service.FoodOptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FoodOptionServiceImpl implements FoodOptionService {

    private final FoodOptionRepository foodOptionRepository;
    private final MealRepository mealRepository;

    @Override
    @Transactional
    public FoodOptionResponseDTO addFoodOptionToMeal(Long mealId, FoodOptionRequestDTO request) {
        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new MealNotFoundException("Meal not found with id: " + mealId));

        FoodOption foodOption = FoodOption.builder()
                .meal(meal)
                .name(request.getName().trim())
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .build();

        meal.addFoodOption(foodOption);
        FoodOption saved = foodOptionRepository.save(foodOption);

        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FoodOptionResponseDTO> getFoodOptionsForMeal(Long mealId) {
        if (!mealRepository.existsById(mealId)) {
            throw new MealNotFoundException("Meal not found with id: " + mealId);
        }

        return foodOptionRepository.findByMealId(mealId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FoodOptionResponseDTO updateFoodOption(Long id, FoodOptionRequestDTO request) {
        FoodOption foodOption = foodOptionRepository.findById(id)
                .orElseThrow(() -> new FoodOptionNotFoundException("Food option not found with id: " + id));

        foodOption.setName(request.getName().trim());
        if (request.getDescription() != null) {
            foodOption.setDescription(request.getDescription().trim());
        }

        FoodOption updated = foodOptionRepository.save(foodOption);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteFoodOption(Long id) {
        FoodOption foodOption = foodOptionRepository.findById(id)
                .orElseThrow(() -> new FoodOptionNotFoundException("Food option not found with id: " + id));

        if (foodOption.getMeal() != null) {
            foodOption.getMeal().removeFoodOption(foodOption);
        }
        foodOptionRepository.delete(foodOption);
    }

    private FoodOptionResponseDTO mapToDTO(FoodOption option) {
        return FoodOptionResponseDTO.builder()
                .id(option.getId())
                .mealId(option.getMeal() != null ? option.getMeal().getId() : null)
                .name(option.getName())
                .description(option.getDescription())
                .createdAt(option.getCreatedAt())
                .build();
    }
}
