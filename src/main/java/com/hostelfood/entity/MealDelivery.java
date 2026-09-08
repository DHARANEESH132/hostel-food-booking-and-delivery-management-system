package com.hostelfood.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "meal_deliveries",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_delivery_token", columnNames = {"meal_token_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_token_id", nullable = false)
    private MealToken mealToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id", nullable = false)
    private Meal meal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_option_id", nullable = false)
    private FoodOption foodOption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivered_by_user_id", nullable = false)
    private User deliveredBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime deliveredAt;

    @Column(length = 255)
    private String notes;
}
