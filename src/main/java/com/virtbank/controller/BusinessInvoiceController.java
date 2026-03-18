package com.virtbank.controller;

import com.virtbank.dto.ApiResponse;
import com.virtbank.dto.CreateInvoiceRequest;
import com.virtbank.dto.InvoiceResponse;
import com.virtbank.dto.UpdateTicketStatusRequest;
import com.virtbank.service.BusinessInvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/business/invoices")
@PreAuthorize("hasRole('BUSINESS')")
@RequiredArgsConstructor
public class BusinessInvoiceController {

    private final BusinessInvoiceService businessInvoiceService;

    @PostMapping
    public ResponseEntity<ApiResponse<InvoiceResponse>> createInvoice(
            @Valid @RequestBody CreateInvoiceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Invoice created",
                businessInvoiceService.createInvoice(request)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<InvoiceResponse>> updateInvoiceStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateTicketStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Invoice status updated",
                businessInvoiceService.updateInvoiceStatus(id, request.getStatus())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<InvoiceResponse>>> getInvoices(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(businessInvoiceService.getInvoices(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(businessInvoiceService.getInvoiceById(id)));
    }
}
