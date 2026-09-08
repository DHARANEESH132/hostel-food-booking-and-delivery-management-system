package com.hostelfood.repository;

import com.hostelfood.entity.MealToken;
import com.hostelfood.enums.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MealTokenRepository extends JpaRepository<MealToken, Long> {

    Optional<MealToken> findByTokenCode(String tokenCode);

    Optional<MealToken> findByUserIdAndMealId(Long userId, Long mealId);

    boolean existsByUserIdAndMealId(Long userId, Long mealId);

    List<MealToken> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<MealToken> findByMealId(Long mealId);

    long countByMealIdAndStatus(Long mealId, TokenStatus status);

    long countByStatus(TokenStatus status);

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, TokenStatus status);
}
