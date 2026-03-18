package com.virtbank.controller;

import com.virtbank.dto.AccountResponse;
import com.virtbank.dto.ApiResponse;
import com.virtbank.service.AdminAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/accounts")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AccountResponse>>> getAllAccounts(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(adminAccountService.getAllAccounts(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminAccountService.getAccountById(id)));
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<ApiResponse<AccountResponse>> closeAccount(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Account closed",
                adminAccountService.closeAccount(id)));
    }
}
