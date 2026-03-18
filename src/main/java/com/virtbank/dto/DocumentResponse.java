package com.virtbank.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private Long id;
    private Long userId;
    private String fileName;
    private String documentType;
    private String fileUrl;
    private Long fileSize;
    private String mimeType;
    private LocalDateTime createdAt;
}
