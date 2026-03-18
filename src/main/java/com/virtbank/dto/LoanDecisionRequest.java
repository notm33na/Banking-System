package com.virtbank.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class LoanDecisionRequest {
    @NotBlank(message = "Decision is required (APPROVED or REJECTED)")
    private String decision;
    private String rejectionReason;
}
