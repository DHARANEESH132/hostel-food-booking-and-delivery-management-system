package com.hostelfood.dto.auth;

import com.hostelfood.dto.user.UserSummaryDTO;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO {

    private String token;
    private String tokenType;
    private UserSummaryDTO user;
}
