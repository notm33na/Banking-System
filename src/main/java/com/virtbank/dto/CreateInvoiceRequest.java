package com.virtbank.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class CreateInvoiceRequest {
    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Customer email is required")
    @Email
    private String customerEmail;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private String notes;

    @NotEmpty(message = "At least one invoice item is required")
    private List<InvoiceItemRequest> items;
}
