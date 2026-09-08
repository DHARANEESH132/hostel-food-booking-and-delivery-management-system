package com.hostelfood.controller;

import com.hostelfood.dto.report.DateRangeReportDTO;
import com.hostelfood.dto.report.MealConsumptionReportDTO;
import com.hostelfood.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;

    @GetMapping("/meals/{mealId}")
    public ResponseEntity<MealConsumptionReportDTO> getMealReport(@PathVariable Long mealId) {
        return ResponseEntity.ok(reportService.getMealConsumptionReport(mealId));
    }

    @GetMapping("/date-range")
    public ResponseEntity<DateRangeReportDTO> getDateRangeReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(reportService.getDateRangeReport(startDate, endDate));
    }
}
