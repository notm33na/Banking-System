package com.virtbank.controller;

import com.virtbank.dto.ApiResponse;
import com.virtbank.dto.DocumentResponse;
import com.virtbank.dto.KycResponse;
import com.virtbank.service.CustomerDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/customer/documents")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
public class CustomerDocumentController {

    private final CustomerDocumentService customerDocumentService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType) throws IOException {
        return ResponseEntity.ok(ApiResponse.success("Document uploaded",
                customerDocumentService.uploadDocument(file, documentType)));
    }

    @PostMapping("/kyc/upload")
    public ResponseEntity<ApiResponse<KycResponse>> uploadKycDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType,
            @RequestParam("documentNumber") String documentNumber) throws IOException {
        return ResponseEntity.ok(ApiResponse.success("KYC document uploaded",
                customerDocumentService.uploadKycDocument(file, documentType, documentNumber)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getMyDocuments() {
        return ResponseEntity.ok(ApiResponse.success(customerDocumentService.getMyDocuments()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocumentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customerDocumentService.getDocumentById(id)));
    }

    @GetMapping("/kyc/status")
    public ResponseEntity<ApiResponse<List<KycResponse>>> getKycStatus() {
        return ResponseEntity.ok(ApiResponse.success(customerDocumentService.getMyKycStatus()));
    }
}
