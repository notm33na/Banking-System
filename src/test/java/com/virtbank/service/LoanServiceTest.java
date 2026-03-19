package com.virtbank.service;

import com.virtbank.entity.Account;
import com.virtbank.entity.Loan;
import com.virtbank.entity.LoanPayment;
import com.virtbank.entity.User;
import com.virtbank.entity.enums.LoanStatus;
import com.virtbank.entity.enums.LoanType;
import com.virtbank.exception.ResourceNotFoundException;
import com.virtbank.repository.LoanPaymentRepository;
import com.virtbank.repository.LoanRepository;
import com.virtbank.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock private LoanRepository loanRepository;
    @Mock private LoanPaymentRepository loanPaymentRepository;
    @Mock private SecurityUtils securityUtils;
    @Mock private EmailService emailService;
    @Mock private NotificationService notificationService;

    @InjectMocks private AdminLoanService loanService;

    private User borrower;
    private Account account;
    private Loan pendingLoan;

    @BeforeEach
    void setUp() {
        borrower = User.builder().id(1L).firstName("John").lastName("Doe")
                .email("john@test.com").build();
        account = Account.builder().id(1L).user(borrower).build();

        pendingLoan = Loan.builder()
                .id(1L).user(borrower).account(account)
                .loanType(LoanType.PERSONAL)
                .amount(new BigDecimal("10000.00"))
                .interestRate(new BigDecimal("5.0"))
                .termMonths(12).status(LoanStatus.PENDING)
                .purpose("Test loan").build();
    }

    @Test
    void approveLoan_changesStatusAndGeneratesSchedule() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(pendingLoan));
        when(securityUtils.getCurrentUser()).thenReturn(borrower);
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = loanService.makeLoanDecision(1L, "APPROVED");

        assertEquals("ACTIVE", response.getStatus());
        assertNotNull(pendingLoan.getMonthlyPayment());

        // Verify schedule generated with correct # of instalments
        ArgumentCaptor<List<LoanPayment>> captor = ArgumentCaptor.forClass(List.class);
        verify(loanPaymentRepository).saveAll(captor.capture());
        assertEquals(12, captor.getValue().size());

        // Verify email sent
        verify(emailService).sendLoanStatusEmail("john@test.com", "John Doe",
                "APPROVED", new BigDecimal("10000.00"));
        verify(notificationService).createNotification(eq(1L), eq("LOAN"), anyString());
    }

    @Test
    void rejectLoan_changesStatusAndSendsEmail() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(pendingLoan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = loanService.makeLoanDecision(1L, "REJECTED");

        assertEquals("REJECTED", response.getStatus());
        verify(emailService).sendLoanStatusEmail("john@test.com", "John Doe",
                "REJECTED", new BigDecimal("10000.00"));
        verify(loanPaymentRepository, never()).saveAll(any());
    }

    @Test
    void approveLoan_alreadyApproved_throwsException() {
        pendingLoan.setStatus(LoanStatus.ACTIVE);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(pendingLoan));

        assertThrows(IllegalArgumentException.class,
                () -> loanService.makeLoanDecision(1L, "APPROVED"));
    }

    @Test
    void approveLoan_notFound_throwsResourceNotFound() {
        when(loanRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> loanService.makeLoanDecision(999L, "APPROVED"));
    }
}
