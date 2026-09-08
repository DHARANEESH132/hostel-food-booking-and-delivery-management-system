package com.hostelfood.service;

import com.hostelfood.dto.dashboard.AdminDashboardDTO;
import com.hostelfood.dto.dashboard.StudentDashboardDTO;

public interface DashboardService {

    AdminDashboardDTO getAdminDashboard();

    StudentDashboardDTO getStudentDashboard(String studentEmail);
}
