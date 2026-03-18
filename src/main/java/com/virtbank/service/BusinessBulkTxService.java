package com.virtbank.service;

import com.virtbank.dto.BulkPaymentRequest;
import com.virtbank.dto.BulkTxResponse;
import com.virtbank.entity.*;
import com.virtbank.entity.enums.BulkTransactionItemStatus;
import com.virtbank.entity.enums.BulkTransactionStatus;
import com.virtbank.exception.ResourceNotFoundException;
import com.virtbank.repository.AccountRepository;
import com.virtbank.repository.BulkTransactionRepository;
import com.virtbank.repository.BusinessRepository;
import com.virtbank.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BusinessBulkTxService {

    private final BulkTransactionRepository bulkTxRepository;
    private final BusinessRepository businessRepository;
    private final AccountRepository accountRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public BulkTxResponse submitBulkPayment(BulkPaymentRequest req) {
        Business business = getOwnBusiness();
        User initiator = securityUtils.getCurrentUser();

        Account sourceAccount = accountRepository.findById(req.getSourceAccountId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Source account not found: " + req.getSourceAccountId()));
        securityUtils.assertOwnership(sourceAccount.getUser().getId());

        BigDecimal totalAmount = req.getItems().stream()
                .map(i -> i.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);

        BulkTransaction bulkTx = BulkTransaction.builder()
                .business(business).initiatedByUser(initiator)
                .description(req.getDescription())
                .totalAmount(totalAmount)
                .totalCount(req.getItems().size())
                .status(BulkTransactionStatus.PENDING)
                .build();

        int successCount = 0;
        int failedCount = 0;

        for (var item : req.getItems()) {
            BulkTransactionItem txItem = BulkTransactionItem.builder()
                    .bulkTransaction(bulkTx)
                    .recipientAccount(item.getRecipientAccount())
                    .recipientName(item.getRecipientName())
                    .amount(item.getAmount())
                    .reference(item.getReference())
                    .build();

            // Attempt to process each item
            if (sourceAccount.getBalance().compareTo(item.getAmount()) >= 0) {
                sourceAccount.setBalance(sourceAccount.getBalance().subtract(item.getAmount()));
                txItem.setStatus(BulkTransactionItemStatus.COMPLETED);
                successCount++;
            } else {
                txItem.setStatus(BulkTransactionItemStatus.FAILED);
                failedCount++;
            }
            bulkTx.getItems().add(txItem);
        }

        accountRepository.save(sourceAccount);
        bulkTx.setStatus(BulkTransactionStatus.COMPLETED);
        bulkTx.setProcessedAt(LocalDateTime.now());
        BulkTransaction saved = bulkTxRepository.save(bulkTx);

        return BulkTxResponse.builder()
                .id(saved.getId()).businessId(business.getId())
                .description(saved.getDescription())
                .totalAmount(totalAmount)
                .totalCount(req.getItems().size())
                .successCount(successCount).failedCount(failedCount)
                .status(saved.getStatus().name())
                .createdAt(saved.getCreatedAt())
                .processedAt(saved.getProcessedAt())
                .build();
    }

    public Page<BulkTxResponse> getBulkTxHistory(Pageable pageable) {
        Business business = getOwnBusiness();
        return bulkTxRepository.findByBusinessId(business.getId(), pageable).map(bt ->
                BulkTxResponse.builder()
                        .id(bt.getId()).businessId(business.getId())
                        .description(bt.getDescription())
                        .totalAmount(bt.getTotalAmount())
                        .totalCount(bt.getTotalCount())
                        .status(bt.getStatus().name())
                        .createdAt(bt.getCreatedAt())
                        .processedAt(bt.getProcessedAt())
                        .build());
    }

    private Business getOwnBusiness() {
        Long userId = securityUtils.getCurrentUserId();
        return businessRepository.findByOwnerId(userId).stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No business found"));
    }
}
