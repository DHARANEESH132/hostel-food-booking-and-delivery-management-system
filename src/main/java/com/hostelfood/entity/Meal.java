package com.hostelfood.entity;

import com.hostelfood.enums.MealStatus;
import com.hostelfood.enums.MealType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MealType mealType;

    @Column(nullable = false)
    private LocalDateTime votingStartTime;

    @Column(nullable = false)
    private LocalDateTime votingEndTime;

    @Column(nullable = false)
    private LocalDateTime deliveryStartTime;

    @Column(nullable = false)
    private LocalDateTime deliveryEndTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MealStatus status;

    @OneToMany(mappedBy = "meal", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FoodOption> foodOptions = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void addFoodOption(FoodOption foodOption) {
        foodOptions.add(foodOption);
        foodOption.setMeal(this);
    }

    public void removeFoodOption(FoodOption foodOption) {
        foodOptions.remove(foodOption);
        foodOption.setMeal(null);
    }
}
