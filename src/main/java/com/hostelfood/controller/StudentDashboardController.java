package com.hostelfood.controller;

import com.hostelfood.dto.dashboard.StudentDashboardDTO;
import com.hostelfood.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/dashboard")
@RequiredArgsConstructor
public class StudentDashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<StudentDashboardDTO> getDashboard(Authentication authentication) {
        return ResponseEntity.ok(dashboardService.getStudentDashboard(authentication.getName()));
    }
}
