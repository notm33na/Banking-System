package com.virtbank.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class UpdateTicketStatusRequest {
    @NotBlank(message = "Status is required (OPEN, IN_PROGRESS, RESOLVED, or CLOSED)")
    private String status;
}
