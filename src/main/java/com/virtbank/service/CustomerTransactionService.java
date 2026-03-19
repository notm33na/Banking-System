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
import com.virtbank.service.EmailService;
import com.virtbank.service.NotificationService;
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
    private final EmailService emailService;
    private final NotificationService notificationService;

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
        TransactionResponse response = toResponse(transactionRepository.save(tx));

        // Send alerts
        String userName = account.getUser().getFirstName() + " " + account.getUser().getLastName();
        String email = account.getUser().getEmail();
        emailService.sendTransactionAlert(email, userName, "DEPOSIT", req.getAmount(), account.getBalance());
        notificationService.createNotification(account.getUser().getId(), "TRANSACTION",
                "Deposit of " + req.getAmount() + " processed successfully.");

        return response;
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
        TransactionResponse response = toResponse(transactionRepository.save(tx));

        // Send alerts
        String userName = account.getUser().getFirstName() + " " + account.getUser().getLastName();
        String email = account.getUser().getEmail();
        emailService.sendTransactionAlert(email, userName, "WITHDRAWAL", req.getAmount(), account.getBalance());
        notificationService.createNotification(account.getUser().getId(), "TRANSACTION",
                "Withdrawal of " + req.getAmount() + " processed successfully.");

        return response;
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
        TransactionResponse response = toResponse(transactionRepository.save(tx));

        // Send alerts to sender
        String senderName = source.getUser().getFirstName() + " " + source.getUser().getLastName();
        emailService.sendTransactionAlert(source.getUser().getEmail(), senderName,
                "TRANSFER (SENT)", req.getAmount(), source.getBalance());
        notificationService.createNotification(source.getUser().getId(), "TRANSACTION",
                "Transfer of " + req.getAmount() + " sent successfully.");

        // Send alerts to receiver
        if (dest.getUser() != null) {
            String receiverName = dest.getUser().getFirstName() + " " + dest.getUser().getLastName();
            emailService.sendTransactionAlert(dest.getUser().getEmail(), receiverName,
                    "TRANSFER (RECEIVED)", req.getAmount(), dest.getBalance());
            notificationService.createNotification(dest.getUser().getId(), "TRANSACTION",
                    "Transfer of " + req.getAmount() + " received.");
        }

        return response;
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
