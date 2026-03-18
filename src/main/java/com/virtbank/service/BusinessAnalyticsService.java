package com.virtbank.service;

import com.virtbank.dto.AnalyticsSummary;
import com.virtbank.dto.CashflowEntry;
import com.virtbank.dto.ExpenseCategory;
import com.virtbank.entity.Account;
import com.virtbank.entity.Business;
import com.virtbank.entity.Transaction;
import com.virtbank.entity.enums.TransactionType;
import com.virtbank.exception.ResourceNotFoundException;
import com.virtbank.repository.AccountRepository;
import com.virtbank.repository.BusinessRepository;
import com.virtbank.repository.TransactionRepository;
import com.virtbank.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusinessAnalyticsService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final BusinessRepository businessRepository;
    private final SecurityUtils securityUtils;

    public AnalyticsSummary getIncomeVsExpense(LocalDateTime start, LocalDateTime end) {
        List<Long> accountIds = getBusinessAccountIds();

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (Long accountId : accountIds) {
            List<Transaction> transactions = transactionRepository.findByAccountIdAndDateRange(accountId, start, end);
            for (Transaction tx : transactions) {
                if (tx.getTransactionType() == TransactionType.DEPOSIT
                        || tx.getTransactionType() == TransactionType.TRANSFER
                        && tx.getDestinationAccount() != null
                        && accountIds.contains(tx.getDestinationAccount().getId())) {
                    totalIncome = totalIncome.add(tx.getAmount());
                } else if (tx.getTransactionType() == TransactionType.WITHDRAWAL
                        || tx.getTransactionType() == TransactionType.PAYMENT) {
                    totalExpenses = totalExpenses.add(tx.getAmount());
                }
            }
        }

        return AnalyticsSummary.builder()
                .totalIncome(totalIncome).totalExpenses(totalExpenses)
                .netCashflow(totalIncome.subtract(totalExpenses))
                .build();
    }

    public List<CashflowEntry> getCashflowByMonth(LocalDateTime start, LocalDateTime end) {
        List<Long> accountIds = getBusinessAccountIds();
        Map<String, CashflowEntry> monthlyMap = new TreeMap<>();

        for (Long accountId : accountIds) {
            List<Transaction> transactions = transactionRepository.findByAccountIdAndDateRange(accountId, start, end);
            for (Transaction tx : transactions) {
                String key = tx.getCreatedAt().getYear() + "-" +
                        String.format("%02d", tx.getCreatedAt().getMonthValue());

                CashflowEntry entry = monthlyMap.computeIfAbsent(key,
                        k -> CashflowEntry.builder()
                                .year(tx.getCreatedAt().getYear())
                                .month(tx.getCreatedAt().getMonthValue())
                                .income(BigDecimal.ZERO).expense(BigDecimal.ZERO)
                                .net(BigDecimal.ZERO).build());

                if (tx.getTransactionType() == TransactionType.DEPOSIT) {
                    entry.setIncome(entry.getIncome().add(tx.getAmount()));
                } else if (tx.getTransactionType() == TransactionType.WITHDRAWAL
                        || tx.getTransactionType() == TransactionType.PAYMENT) {
                    entry.setExpense(entry.getExpense().add(tx.getAmount()));
                }
                entry.setNet(entry.getIncome().subtract(entry.getExpense()));
            }
        }
        return new ArrayList<>(monthlyMap.values());
    }

    public List<ExpenseCategory> getTopExpenseCategories(LocalDateTime start, LocalDateTime end) {
        List<Long> accountIds = getBusinessAccountIds();
        Map<String, BigDecimal> categoryMap = new HashMap<>();
        Map<String, Long> categoryCount = new HashMap<>();

        for (Long accountId : accountIds) {
            List<Transaction> transactions = transactionRepository.findByAccountIdAndDateRange(accountId, start, end);
            for (Transaction tx : transactions) {
                if (tx.getTransactionType() == TransactionType.WITHDRAWAL
                        || tx.getTransactionType() == TransactionType.PAYMENT) {
                    String cat = tx.getDescription() != null ? tx.getDescription() : "Uncategorized";
                    categoryMap.merge(cat, tx.getAmount(), BigDecimal::add);
                    categoryCount.merge(cat, 1L, Long::sum);
                }
            }
        }

        return categoryMap.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(10)
                .map(e -> ExpenseCategory.builder()
                        .category(e.getKey())
                        .totalAmount(e.getValue())
                        .transactionCount(categoryCount.getOrDefault(e.getKey(), 0L))
                        .build())
                .collect(Collectors.toList());
    }

    private List<Long> getBusinessAccountIds() {
        Business business = getOwnBusiness();
        return accountRepository.findByUserId(business.getOwner().getId())
                .stream().map(Account::getId).collect(Collectors.toList());
    }

    private Business getOwnBusiness() {
        Long userId = securityUtils.getCurrentUserId();
        return businessRepository.findByOwnerId(userId).stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No business found"));
    }
}
