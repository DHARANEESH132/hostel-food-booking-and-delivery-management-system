package com.hostelfood.dto.user;

import com.hostelfood.enums.Role;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    private Long id;
    private String name;
    private String studentId;
    private String email;
    private Role role;
    private String hostel;
    private String roomNumber;
    private LocalDateTime createdAt;
}
