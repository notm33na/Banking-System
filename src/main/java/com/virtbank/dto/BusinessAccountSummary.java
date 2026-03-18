package com.virtbank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BusinessAccountSummary {
    private Long businessId;
    private String businessName;
    private int totalAccounts;
    private BigDecimal aggregateBalance;
}
