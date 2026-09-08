package com.hostelfood.repository;

import com.hostelfood.entity.MealDelivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MealDeliveryRepository extends JpaRepository<MealDelivery, Long> {

    boolean existsByMealTokenId(Long mealTokenId);

    Optional<MealDelivery> findByMealTokenId(Long mealTokenId);

    List<MealDelivery> findByUserIdOrderByDeliveredAtDesc(Long userId);

    Page<MealDelivery> findByUserId(Long userId, Pageable pageable);

    List<MealDelivery> findByMealIdOrderByDeliveredAtDesc(Long mealId);

    Page<MealDelivery> findByMealId(Long mealId, Pageable pageable);

    Page<MealDelivery> findAllByOrderByDeliveredAtDesc(Pageable pageable);

    long countByMealId(Long mealId);

    long countByMealIdAndFoodOptionId(Long mealId, Long foodOptionId);

    long countByDeliveredAtBetween(LocalDateTime start, LocalDateTime end);

    long countByUserId(Long userId);
}
