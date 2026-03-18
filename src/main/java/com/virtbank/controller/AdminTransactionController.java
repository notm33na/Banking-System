package com.virtbank.controller;

import com.virtbank.dto.ApiResponse;
import com.virtbank.dto.TransactionResponse;
import com.virtbank.service.AdminTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/transactions")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminTransactionController {

    private final AdminTransactionService adminTransactionService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getAllTransactions(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(adminTransactionService.getAllTransactions(pageable)));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> filterByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(ApiResponse.success(
                adminTransactionService.getTransactionsByDateRange(start, end)));
    }

    @PutMapping("/{id}/flag")
    public ResponseEntity<ApiResponse<TransactionResponse>> flagTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Transaction flagged",
                adminTransactionService.flagTransaction(id)));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<TransactionResponse>> approveTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Transaction approved",
                adminTransactionService.approveTransaction(id)));
    }

    @GetMapping("/flagged")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getFlaggedTransactions() {
        return ResponseEntity.ok(ApiResponse.success(adminTransactionService.getFlaggedTransactions()));
    }
}
