package com.virtbank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BulkTxResponse {
    private Long id;
    private Long businessId;
    private String description;
    private BigDecimal totalAmount;
    private int totalCount;
    private int successCount;
    private int failedCount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
