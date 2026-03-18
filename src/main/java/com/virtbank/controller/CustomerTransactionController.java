package com.virtbank.controller;

import com.virtbank.dto.*;
import com.virtbank.service.CustomerTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/transactions")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
public class CustomerTransactionController {

    private final CustomerTransactionService customerTransactionService;

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(@Valid @RequestBody DepositRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Deposit successful",
                customerTransactionService.deposit(request)));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(@Valid @RequestBody WithdrawRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Withdrawal successful",
                customerTransactionService.withdraw(request)));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Transfer successful",
                customerTransactionService.transfer(request)));
    }

    @PostMapping("/schedule")
    public ResponseEntity<ApiResponse<TransactionResponse>> schedulePayment(
            @Valid @RequestBody ScheduledPaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment scheduled",
                customerTransactionService.schedulePayment(request)));
    }

    @GetMapping("/history/{accountId}")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactionHistory(
            @PathVariable Long accountId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                customerTransactionService.getMyTransactions(accountId, pageable)));
    }
}
