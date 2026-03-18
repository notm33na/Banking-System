package com.virtbank.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class CreatePayrollRequest {
    @NotNull(message = "Account ID is required for funding")
    private Long accountId;

    @NotNull(message = "Pay period start is required")
    private LocalDate payPeriodStart;

    @NotNull(message = "Pay period end is required")
    private LocalDate payPeriodEnd;

    @NotEmpty(message = "At least one payroll item is required")
    private List<PayrollItemRequest> items;
}
