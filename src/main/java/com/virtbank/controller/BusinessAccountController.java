package com.virtbank.controller;

import com.virtbank.dto.AccountResponse;
import com.virtbank.dto.ApiResponse;
import com.virtbank.dto.BusinessAccountSummary;
import com.virtbank.service.BusinessAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/business/accounts")
@PreAuthorize("hasRole('BUSINESS')")
@RequiredArgsConstructor
public class BusinessAccountController {

    private final BusinessAccountService businessAccountService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getBusinessAccounts() {
        return ResponseEntity.ok(ApiResponse.success(businessAccountService.getBusinessAccounts()));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<BusinessAccountSummary>> getAccountSummary() {
        return ResponseEntity.ok(ApiResponse.success(businessAccountService.getBusinessAccountSummary()));
    }
}
