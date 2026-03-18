package com.virtbank.controller;

import com.virtbank.dto.ApiResponse;
import com.virtbank.dto.KycDecisionRequest;
import com.virtbank.dto.KycResponse;
import com.virtbank.service.AdminKycService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/kyc")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminKycController {

    private final AdminKycService adminKycService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<KycResponse>>> getAllKycDocuments(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(adminKycService.getAllKycDocuments(pageable)));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<KycResponse>>> getPendingDocuments() {
        return ResponseEntity.ok(ApiResponse.success(adminKycService.getPendingKycDocuments()));
    }

    @GetMapping("/expired")
    public ResponseEntity<ApiResponse<List<KycResponse>>> getExpiredDocuments() {
        return ResponseEntity.ok(ApiResponse.success(adminKycService.getExpiredKycDocuments()));
    }

    @PutMapping("/{id}/decision")
    public ResponseEntity<ApiResponse<KycResponse>> updateKycStatus(
            @PathVariable Long id, @Valid @RequestBody KycDecisionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("KYC status updated",
                adminKycService.updateKycStatus(id, request.getStatus(), request.getRejectionReason())));
    }
}
