package com.virtbank.controller;

import com.virtbank.dto.*;
import com.virtbank.service.BusinessLoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/business/loans")
@PreAuthorize("hasRole('BUSINESS')")
@RequiredArgsConstructor
public class BusinessLoanController {

    private final BusinessLoanService businessLoanService;

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<LoanResponse>> applyForLoan(
            @Valid @RequestBody LoanApplicationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Business loan application submitted",
                businessLoanService.applyForBusinessLoan(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getMyBusinessLoans() {
        return ResponseEntity.ok(ApiResponse.success(businessLoanService.getMyBusinessLoans()));
    }

    @GetMapping("/{loanId}/schedule")
    public ResponseEntity<ApiResponse<List<LoanPaymentResponse>>> getRepaymentSchedule(
            @PathVariable Long loanId) {
        return ResponseEntity.ok(ApiResponse.success(businessLoanService.getRepaymentSchedule(loanId)));
    }
}
