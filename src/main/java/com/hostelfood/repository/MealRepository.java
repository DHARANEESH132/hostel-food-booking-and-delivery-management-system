package com.hostelfood.repository;

import com.hostelfood.entity.Meal;
import com.hostelfood.enums.MealStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {

    List<Meal> findAllByOrderByDateDescVotingStartTimeDesc();

    List<Meal> findByDate(LocalDate date);

    long countByDate(LocalDate date);

    List<Meal> findByStatus(MealStatus status);

    List<Meal> findByDateBetweenOrderByDateAscVotingStartTimeAsc(LocalDate startDate, LocalDate endDate);
}
