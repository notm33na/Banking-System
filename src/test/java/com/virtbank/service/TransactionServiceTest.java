package com.virtbank.service;

import com.virtbank.dto.*;
import com.virtbank.entity.Account;
import com.virtbank.entity.User;
import com.virtbank.entity.Transaction;
import com.virtbank.entity.enums.TransactionStatus;
import com.virtbank.entity.enums.TransactionType;
import com.virtbank.exception.InsufficientFundsException;
import com.virtbank.exception.ResourceNotFoundException;
import com.virtbank.repository.AccountRepository;
import com.virtbank.repository.TransactionRepository;
import com.virtbank.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private SecurityUtils securityUtils;
    @Mock private EmailService emailService;
    @Mock private NotificationService notificationService;

    @InjectMocks private CustomerTransactionService transactionService;

    private User testUser;
    private Account sourceAccount;
    private Account destAccount;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).firstName("John").lastName("Doe")
                .email("john@test.com").build();

        sourceAccount = Account.builder().id(1L).user(testUser)
                .balance(new BigDecimal("5000.00")).currency("USD").build();

        destAccount = Account.builder().id(2L)
                .user(User.builder().id(2L).firstName("Jane").lastName("Smith")
                        .email("jane@test.com").build())
                .balance(new BigDecimal("1000.00")).currency("USD").build();
    }

    // ── Transfer Tests ───────────────────────────────────────────────

    @Test
    void transfer_success_updatesBalancesAndSavesTransaction() {
        TransferRequest req = new TransferRequest();
        req.setSourceAccountId(1L);
        req.setDestinationAccountId(2L);
        req.setAmount(new BigDecimal("500.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(destAccount));
        doNothing().when(securityUtils).assertOwnership(1L);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        TransactionResponse response = transactionService.transfer(req);

        assertNotNull(response);
        assertEquals(new BigDecimal("4500.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("1500.00"), destAccount.getBalance());
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void transfer_insufficientFunds_throwsException() {
        TransferRequest req = new TransferRequest();
        req.setSourceAccountId(1L);
        req.setDestinationAccountId(2L);
        req.setAmount(new BigDecimal("10000.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(destAccount));
        doNothing().when(securityUtils).assertOwnership(1L);

        assertThrows(InsufficientFundsException.class, () -> transactionService.transfer(req));
        assertEquals(new BigDecimal("5000.00"), sourceAccount.getBalance());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void transfer_nonExistentDestination_throwsResourceNotFound() {
        TransferRequest req = new TransferRequest();
        req.setSourceAccountId(1L);
        req.setDestinationAccountId(999L);
        req.setAmount(new BigDecimal("100.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());
        doNothing().when(securityUtils).assertOwnership(1L);

        assertThrows(ResourceNotFoundException.class, () -> transactionService.transfer(req));
    }

    @Test
    void transfer_success_sendsEmailAndNotification() {
        TransferRequest req = new TransferRequest();
        req.setSourceAccountId(1L);
        req.setDestinationAccountId(2L);
        req.setAmount(new BigDecimal("200.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(destAccount));
        doNothing().when(securityUtils).assertOwnership(1L);
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        transactionService.transfer(req);

        verify(emailService).sendTransactionAlert(
                eq("john@test.com"), eq("John Doe"), eq("TRANSFER (SENT)"),
                eq(new BigDecimal("200.00")), any(BigDecimal.class));
        verify(emailService).sendTransactionAlert(
                eq("jane@test.com"), eq("Jane Smith"), eq("TRANSFER (RECEIVED)"),
                eq(new BigDecimal("200.00")), any(BigDecimal.class));
        verify(notificationService, times(2)).createNotification(anyLong(), eq("TRANSACTION"), anyString());
    }

    // ── Deposit Tests ────────────────────────────────────────────────

    @Test
    void deposit_success_increasesBalance() {
        DepositRequest req = new DepositRequest();
        req.setAccountId(1L);
        req.setAmount(new BigDecimal("1000.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        doNothing().when(securityUtils).assertOwnership(1L);
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse response = transactionService.deposit(req);

        assertNotNull(response);
        assertEquals(new BigDecimal("6000.00"), sourceAccount.getBalance());
    }

    // ── Withdrawal Tests ─────────────────────────────────────────────

    @Test
    void withdraw_success_decreasesBalance() {
        WithdrawRequest req = new WithdrawRequest();
        req.setAccountId(1L);
        req.setAmount(new BigDecimal("500.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        doNothing().when(securityUtils).assertOwnership(1L);
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransactionResponse response = transactionService.withdraw(req);

        assertNotNull(response);
        assertEquals(new BigDecimal("4500.00"), sourceAccount.getBalance());
    }

    @Test
    void withdraw_insufficientFunds_throwsException() {
        WithdrawRequest req = new WithdrawRequest();
        req.setAccountId(1L);
        req.setAmount(new BigDecimal("99999.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        doNothing().when(securityUtils).assertOwnership(1L);

        assertThrows(InsufficientFundsException.class, () -> transactionService.withdraw(req));
    }
}
