package com.finance.manager.service;

import com.finance.manager.dto.response.MonthlyReportResponse;
import com.finance.manager.dto.response.YearlyReportResponse;

/**
 * Service interface for generating financial reports and analytics.
 */
public interface ReportService {

    /**
     * Generates a monthly report showing income, expenses, and net savings by category.
     *
     * @param username authenticated user
     * @param year     the year for the report
     * @param month    the month for the report (1-12)
     * @return monthly report response
     */
    MonthlyReportResponse getMonthlyReport(String username, int year, int month);

    /**
     * Generates a yearly report aggregating all monthly data.
     *
     * @param username authenticated user
     * @param year     the year for the report
     * @return yearly report response
     */
    YearlyReportResponse getYearlyReport(String username, int year);
}
