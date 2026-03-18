package com.virtbank.service;

import com.virtbank.dto.AccountResponse;
import com.virtbank.entity.Account;
import com.virtbank.repository.AccountRepository;
import com.virtbank.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerAccountService {

    private final AccountRepository accountRepository;
    private final SecurityUtils securityUtils;

    public List<AccountResponse> getMyAccounts() {
        Long userId = securityUtils.getCurrentUserId();
        return accountRepository.findByUserId(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public BigDecimal getTotalBalance() {
        Long userId = securityUtils.getCurrentUserId();
        BigDecimal total = accountRepository.getTotalBalanceByUserId(userId);
        return total != null ? total : BigDecimal.ZERO;
    }

    public AccountResponse getMyAccountById(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new com.virtbank.exception.ResourceNotFoundException(
                        "Account not found with id: " + accountId));
        securityUtils.assertOwnership(account.getUser().getId());
        return toResponse(account);
    }

    private AccountResponse toResponse(Account a) {
        return AccountResponse.builder()
                .id(a.getId()).accountNumber(a.getAccountNumber())
                .userId(a.getUser().getId())
                .ownerName(a.getUser().getFirstName() + " " + a.getUser().getLastName())
                .accountType(a.getAccountType().name())
                .status(a.getStatus().name())
                .balance(a.getBalance()).currency(a.getCurrency())
                .accountName(a.getAccountName())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
