package com.hostelfood.repository;

import com.hostelfood.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    boolean existsByUserIdAndMealId(Long userId, Long mealId);

    Optional<Feedback> findByUserIdAndMealId(Long userId, Long mealId);

    Page<Feedback> findByMealId(Long mealId, Pageable pageable);

    List<Feedback> findByMealId(Long mealId);

    Page<Feedback> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByMealId(Long mealId);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.meal.id = :mealId")
    Double findAverageRatingByMealId(@Param("mealId") Long mealId);
}
