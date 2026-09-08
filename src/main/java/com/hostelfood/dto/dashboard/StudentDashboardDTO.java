package com.hostelfood.dto.dashboard;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDashboardDTO {

    private String studentName;
    private String studentRegistrationNumber;
    private String email;
    private String hostel;
    private String roomNumber;
    private long totalVotesCast;
    private long totalMealsReceived;
    private long activeTokensCount;
    private long todayMealsCount;
}
