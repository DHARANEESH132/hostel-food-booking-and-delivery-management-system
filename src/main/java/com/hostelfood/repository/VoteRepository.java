package com.hostelfood.repository;

import com.hostelfood.entity.Vote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    Optional<Vote> findByUserIdAndMealId(Long userId, Long mealId);

    boolean existsByUserIdAndMealId(Long userId, Long mealId);

    long countByMealId(Long mealId);

    long countByFoodOptionId(Long foodOptionId);

    long countByMealIdAndFoodOptionId(Long mealId, Long foodOptionId);

    List<Vote> findByMealId(Long mealId);

    List<Vote> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserId(Long userId);

    Page<Vote> findByUserId(Long userId, Pageable pageable);
}
