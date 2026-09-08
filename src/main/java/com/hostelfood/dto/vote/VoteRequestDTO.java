package com.hostelfood.dto.vote;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoteRequestDTO {

    @NotNull(message = "Food option ID is required")
    private Long foodOptionId;
}
