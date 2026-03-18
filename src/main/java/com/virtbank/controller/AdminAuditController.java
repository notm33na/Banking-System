package com.virtbank.controller;

import com.virtbank.dto.ApiResponse;
import com.virtbank.dto.AuditLogResponse;
import com.virtbank.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/audit")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminAuditController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> getAllLogs(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(auditLogService.getAllLogs(pageable)));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> filterByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                auditLogService.getLogsByDateRange(start, end, pageable)));
    }
}
