package com.virtbank.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class TicketReplyRequest {
    @NotBlank(message = "Message is required")
    private String message;
}
