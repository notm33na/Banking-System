package com.virtbank.controller;

import com.virtbank.dto.ApiResponse;
import com.virtbank.dto.CreatePayrollRequest;
import com.virtbank.dto.PayrollResponse;
import com.virtbank.service.BusinessPayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/business/payroll")
@PreAuthorize("hasRole('BUSINESS')")
@RequiredArgsConstructor
public class BusinessPayrollController {

    private final BusinessPayrollService businessPayrollService;

    @PostMapping
    public ResponseEntity<ApiResponse<PayrollResponse>> createPayroll(
            @Valid @RequestBody CreatePayrollRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payroll created",
                businessPayrollService.createPayroll(request)));
    }

    @PutMapping("/{id}/run")
    public ResponseEntity<ApiResponse<PayrollResponse>> runPayroll(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Payroll processed",
                businessPayrollService.runPayroll(id)));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<PayrollResponse>>> getPayrollHistory(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(businessPayrollService.getPayrollHistory(pageable)));
    }
}
