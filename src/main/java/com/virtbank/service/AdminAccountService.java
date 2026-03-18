package com.virtbank.service;

import com.virtbank.config.Audited;
import com.virtbank.dto.AccountResponse;
import com.virtbank.entity.Account;
import com.virtbank.entity.enums.AccountStatus;
import com.virtbank.exception.ResourceNotFoundException;
import com.virtbank.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAccountService {

    private final AccountRepository accountRepository;

    public Page<AccountResponse> getAllAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable).map(this::toResponse);
    }

    public AccountResponse getAccountById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Audited(action = "CLOSE_ACCOUNT", entityType = "Account")
    @Transactional
    public AccountResponse closeAccount(Long id) {
        Account account = findOrThrow(id);
        account.setStatus(AccountStatus.CLOSED);
        return toResponse(accountRepository.save(account));
    }

    // ── helpers ──────────────────────────────────────────────────────

    private Account findOrThrow(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
    }

    private AccountResponse toResponse(Account a) {
        String ownerName = a.getUser() != null
                ? a.getUser().getFirstName() + " " + a.getUser().getLastName() : null;
        return AccountResponse.builder()
                .id(a.getId()).accountNumber(a.getAccountNumber())
                .userId(a.getUser() != null ? a.getUser().getId() : null)
                .ownerName(ownerName)
                .accountType(a.getAccountType().name())
                .status(a.getStatus().name())
                .balance(a.getBalance()).currency(a.getCurrency())
                .accountName(a.getAccountName())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
