package com.virtbank.service;

import com.virtbank.dto.*;
import com.virtbank.entity.Account;
import com.virtbank.entity.Transaction;
import com.virtbank.entity.enums.TransactionStatus;
import com.virtbank.entity.enums.TransactionType;
import com.virtbank.exception.InsufficientFundsException;
import com.virtbank.exception.ResourceNotFoundException;
import com.virtbank.repository.AccountRepository;
import com.virtbank.repository.TransactionRepository;
import com.virtbank.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerTransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public TransactionResponse deposit(DepositRequest req) {
        Account account = findOwnAccount(req.getAccountId());
        account.setBalance(account.getBalance().add(req.getAmount()));
        accountRepository.save(account);

        Transaction tx = Transaction.builder()
                .referenceNumber(generateRef())
                .account(account)
                .transactionType(TransactionType.DEPOSIT)
                .amount(req.getAmount())
                .balanceAfter(account.getBalance())
                .currency(account.getCurrency())
                .description(req.getDescription() != null ? req.getDescription() : "Deposit")
                .status(TransactionStatus.COMPLETED)
                .build();
        return toResponse(transactionRepository.save(tx));
    }

    @Transactional
    public TransactionResponse withdraw(WithdrawRequest req) {
        Account account = findOwnAccount(req.getAccountId());
        if (account.getBalance().compareTo(req.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient balance for withdrawal");
        }
        account.setBalance(account.getBalance().subtract(req.getAmount()));
        accountRepository.save(account);

        Transaction tx = Transaction.builder()
                .referenceNumber(generateRef())
                .account(account)
                .transactionType(TransactionType.WITHDRAWAL)
                .amount(req.getAmount())
                .balanceAfter(account.getBalance())
                .currency(account.getCurrency())
                .description(req.getDescription() != null ? req.getDescription() : "Withdrawal")
                .status(TransactionStatus.COMPLETED)
                .build();
        return toResponse(transactionRepository.save(tx));
    }

    @Transactional
    public TransactionResponse transfer(TransferRequest req) {
        Account source = findOwnAccount(req.getSourceAccountId());
        Account dest = accountRepository.findById(req.getDestinationAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Destination account not found: " + req.getDestinationAccountId()));

        if (source.getBalance().compareTo(req.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient balance for transfer");
        }

        source.setBalance(source.getBalance().subtract(req.getAmount()));
        dest.setBalance(dest.getBalance().add(req.getAmount()));
        accountRepository.save(source);
        accountRepository.save(dest);

        String ref = generateRef();
        Transaction tx = Transaction.builder()
                .referenceNumber(ref)
                .account(source)
                .transactionType(TransactionType.TRANSFER)
                .amount(req.getAmount())
                .balanceAfter(source.getBalance())
                .currency(source.getCurrency())
                .description(req.getDescription() != null ? req.getDescription() : "Transfer")
                .status(TransactionStatus.COMPLETED)
                .sourceAccount(source)
                .destinationAccount(dest)
                .build();
        return toResponse(transactionRepository.save(tx));
    }

    @Transactional
    public TransactionResponse schedulePayment(ScheduledPaymentRequest req) {
        Account source = findOwnAccount(req.getSourceAccountId());
        // Validate destination exists
        accountRepository.findById(req.getDestinationAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Destination account not found: " + req.getDestinationAccountId()));

        Transaction tx = Transaction.builder()
                .referenceNumber(generateRef())
                .account(source)
                .transactionType(TransactionType.TRANSFER)
                .amount(req.getAmount())
                .currency(source.getCurrency())
                .description(req.getDescription() != null ? req.getDescription() : "Scheduled payment")
                .status(TransactionStatus.PENDING)
                .sourceAccount(source)
                .destinationAccount(accountRepository.findById(req.getDestinationAccountId()).orElse(null))
                .build();
        return toResponse(transactionRepository.save(tx));
    }

    public Page<TransactionResponse> getMyTransactions(Long accountId, Pageable pageable) {
        findOwnAccount(accountId); // ownership check
        return transactionRepository.findByAccountId(accountId, pageable).map(this::toResponse);
    }

    // ── helpers ──────────────────────────────────────────────────────

    private Account findOwnAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));
        securityUtils.assertOwnership(account.getUser().getId());
        return account;
    }

    private String generateRef() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId()).referenceNumber(t.getReferenceNumber())
                .accountId(t.getAccount().getId())
                .transactionType(t.getTransactionType().name())
                .amount(t.getAmount()).balanceAfter(t.getBalanceAfter())
                .currency(t.getCurrency()).description(t.getDescription())
                .status(t.getStatus().name())
                .sourceAccountId(t.getSourceAccount() != null ? t.getSourceAccount().getId() : null)
                .destinationAccountId(t.getDestinationAccount() != null ? t.getDestinationAccount().getId() : null)
                .createdAt(t.getCreatedAt())
                .build();
    }
}
