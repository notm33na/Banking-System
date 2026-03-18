package com.virtbank.controller;

import com.virtbank.dto.*;
import com.virtbank.service.CustomerLoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/loans")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
public class CustomerLoanController {

    private final CustomerLoanService customerLoanService;

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<LoanResponse>> applyForLoan(
            @Valid @RequestBody LoanApplicationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Loan application submitted",
                customerLoanService.applyForLoan(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LoanResponse>>> getMyLoans() {
        return ResponseEntity.ok(ApiResponse.success(customerLoanService.getMyLoans()));
    }

    @GetMapping("/{loanId}/schedule")
    public ResponseEntity<ApiResponse<List<LoanPaymentResponse>>> getRepaymentSchedule(
            @PathVariable Long loanId) {
        return ResponseEntity.ok(ApiResponse.success(customerLoanService.getMyRepaymentSchedule(loanId)));
    }
}
