package com.virtbank.controller;

import com.virtbank.dto.ApiResponse;
import com.virtbank.dto.BulkPaymentRequest;
import com.virtbank.dto.BulkTxResponse;
import com.virtbank.service.BusinessBulkTxService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/business/bulk-transactions")
@PreAuthorize("hasRole('BUSINESS')")
@RequiredArgsConstructor
public class BusinessBulkTxController {

    private final BusinessBulkTxService businessBulkTxService;

    @PostMapping
    public ResponseEntity<ApiResponse<BulkTxResponse>> submitBulkPayment(
            @Valid @RequestBody BulkPaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Bulk payment processed",
                businessBulkTxService.submitBulkPayment(request)));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<BulkTxResponse>>> getBulkTxHistory(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(businessBulkTxService.getBulkTxHistory(pageable)));
    }
}
