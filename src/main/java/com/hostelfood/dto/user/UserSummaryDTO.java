package com.hostelfood.dto.user;

import com.hostelfood.enums.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryDTO {

    private Long id;
    private String name;
    private String email;
    private Role role;
}
