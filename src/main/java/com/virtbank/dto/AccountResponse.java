package com.virtbank.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.virtbank.security.AccountNumberSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private Long id;
    @JsonSerialize(using = AccountNumberSerializer.class)
    private String accountNumber;
    private Long userId;
    private String ownerName;
    private String accountType;
    private String status;
    private BigDecimal balance;
    private String currency;
    private String accountName;
    private LocalDateTime createdAt;
}
