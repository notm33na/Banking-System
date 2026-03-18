package com.virtbank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanPaymentResponse {
    private Long id;
    private Long loanId;
    private BigDecimal amount;
    private BigDecimal principal;
    private BigDecimal interest;
    private LocalDate paymentDate;
    private LocalDate dueDate;
    private String status;
    private LocalDateTime createdAt;
}
