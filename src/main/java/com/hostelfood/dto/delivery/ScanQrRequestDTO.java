package com.hostelfood.dto.delivery;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScanQrRequestDTO {

    @NotBlank(message = "Token code is required")
    private String tokenCode;
}
