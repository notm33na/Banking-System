package com.virtbank.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class UpdateUserStatusRequest {
    @NotBlank(message = "Status is required (ACTIVE, INACTIVE, or SUSPENDED)")
    private String status;
}
