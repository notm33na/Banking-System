package com.virtbank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BusinessMemberResponse {
    private Long id;
    private Long businessId;
    private Long userId;
    private String userName;
    private String email;
    private String role;
    private String status;
    private LocalDateTime createdAt;
}
