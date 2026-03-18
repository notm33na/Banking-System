package com.virtbank.service;

import com.virtbank.config.Audited;
import com.virtbank.dto.TransactionResponse;
import com.virtbank.entity.Transaction;
import com.virtbank.entity.enums.TransactionStatus;
import com.virtbank.exception.ResourceNotFoundException;
import com.virtbank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminTransactionService {

    private final TransactionRepository transactionRepository;

    public Page<TransactionResponse> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable).map(this::toResponse);
    }

    public List<TransactionResponse> getTransactionsByDateRange(LocalDateTime start, LocalDateTime end) {
        // Use a findAll + filter approach; for production you'd add a repo method
        return transactionRepository.findAll().stream()
                .filter(t -> !t.getCreatedAt().isBefore(start) && !t.getCreatedAt().isAfter(end))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Audited(action = "FLAG_TRANSACTION", entityType = "Transaction")
    @Transactional
    public TransactionResponse flagTransaction(Long id) {
        Transaction tx = findOrThrow(id);
        tx.setStatus(TransactionStatus.FLAGGED);
        return toResponse(transactionRepository.save(tx));
    }

    @Audited(action = "APPROVE_TRANSACTION", entityType = "Transaction")
    @Transactional
    public TransactionResponse approveTransaction(Long id) {
        Transaction tx = findOrThrow(id);
        tx.setStatus(TransactionStatus.COMPLETED);
        return toResponse(transactionRepository.save(tx));
    }

    public List<TransactionResponse> getFlaggedTransactions() {
        return transactionRepository
                .findByTypeAndStatus(null, TransactionStatus.FLAGGED)
                .stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── helpers ──────────────────────────────────────────────────────

    private Transaction findOrThrow(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
    }

    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId()).referenceNumber(t.getReferenceNumber())
                .accountId(t.getAccount() != null ? t.getAccount().getId() : null)
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
