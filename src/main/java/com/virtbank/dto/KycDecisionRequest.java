package com.virtbank.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class KycDecisionRequest {
    @NotBlank(message = "Status is required (APPROVED or REJECTED)")
    private String status;
    private String rejectionReason;
}
