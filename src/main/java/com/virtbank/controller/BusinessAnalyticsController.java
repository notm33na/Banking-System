package com.virtbank.controller;

import com.virtbank.dto.*;
import com.virtbank.service.BusinessAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/business/analytics")
@PreAuthorize("hasRole('BUSINESS')")
@RequiredArgsConstructor
public class BusinessAnalyticsController {

    private final BusinessAnalyticsService businessAnalyticsService;

    @GetMapping("/income-expense")
    public ResponseEntity<ApiResponse<AnalyticsSummary>> getIncomeVsExpense(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(ApiResponse.success(
                businessAnalyticsService.getIncomeVsExpense(start, end)));
    }

    @GetMapping("/cashflow")
    public ResponseEntity<ApiResponse<List<CashflowEntry>>> getCashflowByMonth(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(ApiResponse.success(
                businessAnalyticsService.getCashflowByMonth(start, end)));
    }

    @GetMapping("/top-expenses")
    public ResponseEntity<ApiResponse<List<ExpenseCategory>>> getTopExpenseCategories(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(ApiResponse.success(
                businessAnalyticsService.getTopExpenseCategories(start, end)));
    }
}
