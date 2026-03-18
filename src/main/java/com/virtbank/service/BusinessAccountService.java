package com.virtbank.service;

import com.virtbank.dto.AccountResponse;
import com.virtbank.dto.BusinessAccountSummary;
import com.virtbank.entity.Account;
import com.virtbank.entity.Business;
import com.virtbank.exception.ResourceNotFoundException;
import com.virtbank.repository.AccountRepository;
import com.virtbank.repository.BusinessRepository;
import com.virtbank.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusinessAccountService {

    private final BusinessRepository businessRepository;
    private final AccountRepository accountRepository;
    private final SecurityUtils securityUtils;

    public BusinessAccountSummary getBusinessAccountSummary() {
        Business business = getOwnBusiness();
        Long ownerId = business.getOwner().getId();
        List<Account> accounts = accountRepository.findByUserId(ownerId);
        BigDecimal total = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return BusinessAccountSummary.builder()
                .businessId(business.getId())
                .businessName(business.getBusinessName())
                .totalAccounts(accounts.size())
                .aggregateBalance(total)
                .build();
    }

    public List<AccountResponse> getBusinessAccounts() {
        Business business = getOwnBusiness();
        return accountRepository.findByUserId(business.getOwner().getId())
                .stream().map(a -> AccountResponse.builder()
                        .id(a.getId()).accountNumber(a.getAccountNumber())
                        .userId(a.getUser().getId())
                        .accountType(a.getAccountType().name())
                        .status(a.getStatus().name())
                        .balance(a.getBalance()).currency(a.getCurrency())
                        .accountName(a.getAccountName())
                        .createdAt(a.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private Business getOwnBusiness() {
        Long userId = securityUtils.getCurrentUserId();
        return businessRepository.findByOwnerId(userId).stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No business found for the current user"));
    }
}
