package com.finance.manager.controller;

import com.finance.manager.dto.response.MonthlyReportResponse;
import com.finance.manager.dto.response.YearlyReportResponse;
import com.finance.manager.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for financial reports and analytics.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * Generates a monthly report showing income, expenses, and net savings.
     * GET /api/reports/monthly/{year}/{month}
     */
    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable int year,
            @PathVariable int month) {
        MonthlyReportResponse response = reportService.getMonthlyReport(userDetails.getUsername(), year, month);
        return ResponseEntity.ok(response);
    }

    /**
     * Generates a yearly aggregate report.
     * GET /api/reports/yearly/{year}
     */
    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlyReportResponse> getYearlyReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable int year) {
        YearlyReportResponse response = reportService.getYearlyReport(userDetails.getUsername(), year);
        return ResponseEntity.ok(response);
    }
}
