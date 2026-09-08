package com.hostelfood.entity;

import com.hostelfood.enums.TokenStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "meal_tokens",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_token_user_meal", columnNames = {"user_id", "meal_id"})
        },
        indexes = {
                @Index(name = "idx_token_code", columnList = "token_code", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_code", nullable = false, unique = true, length = 100)
    private String tokenCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id", nullable = false)
    private Meal meal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_option_id", nullable = false)
    private FoodOption foodOption;

    @Lob
    @Column(name = "qr_code", nullable = false, columnDefinition = "LONGTEXT")
    private String qrCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TokenStatus status = TokenStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
