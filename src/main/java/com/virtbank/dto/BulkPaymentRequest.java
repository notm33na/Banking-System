package com.virtbank.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class BulkPaymentRequest {
    @NotNull(message = "Source account ID is required")
    private Long sourceAccountId;

    private String description;

    @NotEmpty(message = "At least one payment item is required")
    private List<BulkPaymentItemRequest> items;
}
