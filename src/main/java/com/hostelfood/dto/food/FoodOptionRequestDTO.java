package com.hostelfood.dto.food;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodOptionRequestDTO {

    @NotBlank(message = "Food option name is required")
    private String name;

    private String description;
}
