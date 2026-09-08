package com.hostelfood.dto.food;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodOptionResponseDTO {

    private Long id;
    private Long mealId;
    private String name;
    private String description;
    private LocalDateTime createdAt;
}
