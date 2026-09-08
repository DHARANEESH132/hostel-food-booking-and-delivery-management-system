package com.hostelfood.service.impl;

import com.hostelfood.dto.food.FoodOptionDemandDTO;
import com.hostelfood.dto.meal.MealDemandResponseDTO;
import com.hostelfood.entity.FoodOption;
import com.hostelfood.entity.Meal;
import com.hostelfood.exception.MealNotFoundException;
import com.hostelfood.repository.FoodOptionRepository;
import com.hostelfood.repository.MealRepository;
import com.hostelfood.repository.VoteRepository;
import com.hostelfood.service.DemandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DemandServiceImpl implements DemandService {

    private final MealRepository mealRepository;
    private final FoodOptionRepository foodOptionRepository;
    private final VoteRepository voteRepository;

    @Override
    @Transactional(readOnly = true)
    public MealDemandResponseDTO getMealDemand(Long mealId) {
        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new MealNotFoundException("Meal not found with id: " + mealId));

        long totalVotes = voteRepository.countByMealId(mealId);
        List<FoodOption> foodOptions = foodOptionRepository.findByMealId(mealId);

        List<FoodOptionDemandDTO> optionDemands = new ArrayList<>();
        for (FoodOption option : foodOptions) {
            long count = voteRepository.countByMealIdAndFoodOptionId(mealId, option.getId());
            double percentage = 0.0;
            if (totalVotes > 0) {
                percentage = BigDecimal.valueOf((count * 100.0) / totalVotes)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
            }

            optionDemands.add(FoodOptionDemandDTO.builder()
                    .foodOptionId(option.getId())
                    .foodName(option.getName())
                    .description(option.getDescription())
                    .voteCount(count)
                    .percentage(percentage)
                    .build());
        }

        return MealDemandResponseDTO.builder()
                .mealId(meal.getId())
                .date(meal.getDate())
                .mealType(meal.getMealType())
                .status(meal.getStatus())
                .totalVotes(totalVotes)
                .optionDemands(optionDemands)
                .build();
    }
}
