package com.hostelfood.service.impl;

import com.hostelfood.dto.food.FoodOptionResponseDTO;
import com.hostelfood.dto.meal.MealRequestDTO;
import com.hostelfood.dto.meal.MealResponseDTO;
import com.hostelfood.entity.FoodOption;
import com.hostelfood.entity.Meal;
import com.hostelfood.enums.MealStatus;
import com.hostelfood.exception.MealNotFoundException;
import com.hostelfood.repository.MealRepository;
import com.hostelfood.service.MealService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MealServiceImpl implements MealService {
    private final MealRepository mealRepository;
    @Override
    @Transactional
    public MealResponseDTO createMeal(MealRequestDTO request) {
        MealStatus initialStatus = request.getStatus() != null ? request.getStatus() : computeCurrentStatus(
                request.getVotingStartTime(),
                request.getVotingEndTime(),
                request.getDeliveryStartTime(),
                request.getDeliveryEndTime()
        );

        Meal meal = Meal.builder()
                .date(request.getDate())
                .mealType(request.getMealType())
                .votingStartTime(request.getVotingStartTime())
                .votingEndTime(request.getVotingEndTime())
                .deliveryStartTime(request.getDeliveryStartTime())
                .deliveryEndTime(request.getDeliveryEndTime())
                .status(initialStatus)
                .build();

        Meal savedMeal = mealRepository.save(meal);
        return mapToResponseDTO(savedMeal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MealResponseDTO> getAllMeals() {
        return mealRepository.findAllByOrderByDateDescVotingStartTimeDesc().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MealResponseDTO getMealById(Long id) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new MealNotFoundException("Meal not found with id: " + id));
        return mapToResponseDTO(meal);
    }

    @Override
    @Transactional
    public MealResponseDTO updateMeal(Long id, MealRequestDTO request) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new MealNotFoundException("Meal not found with id: " + id));

        meal.setDate(request.getDate());
        meal.setMealType(request.getMealType());
        meal.setVotingStartTime(request.getVotingStartTime());
        meal.setVotingEndTime(request.getVotingEndTime());
        meal.setDeliveryStartTime(request.getDeliveryStartTime());
        meal.setDeliveryEndTime(request.getDeliveryEndTime());

        if (request.getStatus() != null) {
            meal.setStatus(request.getStatus());
        } else {
            meal.setStatus(computeCurrentStatus(
                    request.getVotingStartTime(),
                    request.getVotingEndTime(),
                    request.getDeliveryStartTime(),
                    request.getDeliveryEndTime()
            ));
        }

        Meal updatedMeal = mealRepository.save(meal);
        return mapToResponseDTO(updatedMeal);
    }

    @Override
    @Transactional
    public void deleteMeal(Long id) {
        if (!mealRepository.existsById(id)) {
            throw new MealNotFoundException("Meal not found with id: " + id);
        }
        mealRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MealResponseDTO> getMealsForStudents() {
        // Students see all upcoming, active, or recent meals
        return mealRepository.findAllByOrderByDateDescVotingStartTimeDesc().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private MealStatus computeCurrentStatus(
            LocalDateTime votingStart,
            LocalDateTime votingEnd,
            LocalDateTime deliveryStart,
            LocalDateTime deliveryEnd) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(votingStart)) {
            return MealStatus.UPCOMING;
        } else if (!now.isAfter(votingEnd)) {
            return MealStatus.VOTING_OPEN;
        } else if (now.isBefore(deliveryStart)) {
            return MealStatus.VOTING_CLOSED;
        } else if (!now.isAfter(deliveryEnd)) {
            return MealStatus.DELIVERY_OPEN;
        } else {
            return MealStatus.COMPLETED;
        }
    }

    private MealResponseDTO mapToResponseDTO(Meal meal) {
        List<FoodOptionResponseDTO> foodOptions = meal.getFoodOptions() != null
                ? meal.getFoodOptions().stream().map(this::mapFoodOptionToDTO).collect(Collectors.toList())
                : List.of();

        return MealResponseDTO.builder()
                .id(meal.getId())
                .date(meal.getDate())
                .mealType(meal.getMealType())
                .votingStartTime(meal.getVotingStartTime())
                .votingEndTime(meal.getVotingEndTime())
                .deliveryStartTime(meal.getDeliveryStartTime())
                .deliveryEndTime(meal.getDeliveryEndTime())
                .status(meal.getStatus())
                .foodOptions(foodOptions)
                .createdAt(meal.getCreatedAt())
                .updatedAt(meal.getUpdatedAt())
                .build();
    }

    private FoodOptionResponseDTO mapFoodOptionToDTO(FoodOption option) {
        return FoodOptionResponseDTO.builder()
                .id(option.getId())
                .mealId(option.getMeal() != null ? option.getMeal().getId() : null)
                .name(option.getName())
                .description(option.getDescription())
                .createdAt(option.getCreatedAt())
                .build();
    }
}
