package com.virtbank.controller;

import com.virtbank.dto.AccountResponse;
import com.virtbank.dto.ApiResponse;
import com.virtbank.service.CustomerAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/customer/accounts")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
public class CustomerAccountController {

    private final CustomerAccountService customerAccountService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getMyAccounts() {
        return ResponseEntity.ok(ApiResponse.success(customerAccountService.getMyAccounts()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> getMyAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerAccountService.getMyAccountById(id)));
    }

    @GetMapping("/total-balance")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalBalance() {
        return ResponseEntity.ok(ApiResponse.success(customerAccountService.getTotalBalance()));
    }
}
