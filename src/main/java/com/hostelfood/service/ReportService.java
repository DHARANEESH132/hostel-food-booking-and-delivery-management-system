package com.hostelfood.service;

import com.hostelfood.dto.report.DateRangeReportDTO;
import com.hostelfood.dto.report.MealConsumptionReportDTO;

import java.time.LocalDate;

public interface ReportService {

    MealConsumptionReportDTO getMealConsumptionReport(Long mealId);

    DateRangeReportDTO getDateRangeReport(LocalDate startDate, LocalDate endDate);
}
