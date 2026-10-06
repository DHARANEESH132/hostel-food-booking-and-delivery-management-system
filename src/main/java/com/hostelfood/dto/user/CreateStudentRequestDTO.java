package com.hostelfood.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateStudentRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Student ID is required")
    private String studentId;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Hostel is required")
    private String hostel;

    @NotBlank(message = "Room number is required")
    private String roomNumber;

    /**
     * Optional initial password. If null or blank, default password (<studentId>@123) is assigned.
     */
    private String password;
}
