package com.hostelfood.dto.food;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodOptionDemandDTO {

    private Long foodOptionId;
    private String foodName;
    private String description;
    private long voteCount;
    private double percentage;
}
