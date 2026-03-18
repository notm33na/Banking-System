package com.virtbank.controller;

import com.virtbank.dto.*;
import com.virtbank.service.AdminLoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/loans")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminLoanController {

    private final AdminLoanService adminLoanService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<LoanResponse>>> getAllLoans(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(adminLoanService.getAllLoans(pageable)));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getPendingLoans() {
        return ResponseEntity.ok(ApiResponse.success(adminLoanService.getPendingLoans()));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getActiveLoans() {
        return ResponseEntity.ok(ApiResponse.success(adminLoanService.getActiveLoans()));
    }

    @PutMapping("/{id}/decision")
    public ResponseEntity<ApiResponse<LoanResponse>> makeLoanDecision(
            @PathVariable Long id, @Valid @RequestBody LoanDecisionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Loan decision processed",
                adminLoanService.makeLoanDecision(id, request.getDecision())));
    }

    @GetMapping("/{id}/schedule")
    public ResponseEntity<ApiResponse<List<LoanPaymentResponse>>> getRepaymentSchedule(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminLoanService.getRepaymentSchedule(id)));
    }
}
