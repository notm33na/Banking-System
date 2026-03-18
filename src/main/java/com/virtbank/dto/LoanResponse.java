package com.virtbank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponse {
    private Long id;
    private Long userId;
    private String borrowerName;
    private Long accountId;
    private String loanType;
    private BigDecimal amount;
    private BigDecimal interestRate;
    private Integer termMonths;
    private BigDecimal monthlyPayment;
    private BigDecimal outstandingBalance;
    private String status;
    private String purpose;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
}
