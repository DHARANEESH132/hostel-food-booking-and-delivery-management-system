package com.hostelfood.repository;

import com.hostelfood.entity.FoodOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodOptionRepository extends JpaRepository<FoodOption, Long> {

    List<FoodOption> findByMealId(Long mealId);

    Optional<FoodOption> findByIdAndMealId(Long id, Long mealId);
}
