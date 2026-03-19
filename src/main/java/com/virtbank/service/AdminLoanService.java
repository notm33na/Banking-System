package com.virtbank.service;

import com.virtbank.config.Audited;
import com.virtbank.dto.LoanPaymentResponse;
import com.virtbank.dto.LoanResponse;
import com.virtbank.entity.Loan;
import com.virtbank.entity.LoanPayment;
import com.virtbank.entity.enums.LoanStatus;
import com.virtbank.entity.enums.PaymentStatus;
import com.virtbank.exception.ResourceNotFoundException;
import com.virtbank.repository.LoanPaymentRepository;
import com.virtbank.repository.LoanRepository;
import com.virtbank.service.EmailService;
import com.virtbank.service.NotificationService;
import com.virtbank.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminLoanService {

    private final LoanRepository loanRepository;
    private final LoanPaymentRepository loanPaymentRepository;
    private final SecurityUtils securityUtils;
    private final EmailService emailService;
    private final NotificationService notificationService;

    public Page<LoanResponse> getAllLoans(Pageable pageable) {
        return loanRepository.findAll(pageable).map(this::toLoanResponse);
    }

    public List<LoanResponse> getPendingLoans() {
        return loanRepository.findPendingLoansOrderByDate()
                .stream().map(this::toLoanResponse).collect(Collectors.toList());
    }

    public List<LoanResponse> getActiveLoans() {
        return loanRepository.findByStatus(LoanStatus.ACTIVE)
                .stream().map(this::toLoanResponse).collect(Collectors.toList());
    }

    @Audited(action = "LOAN_DECISION", entityType = "Loan")
    @Transactional
    public LoanResponse makeLoanDecision(Long loanId, String decision) {
        Loan loan = findOrThrow(loanId);

        if (!"PENDING".equals(loan.getStatus().name())) {
            throw new IllegalArgumentException("Loan is not in PENDING status");
        }

        if ("APPROVED".equalsIgnoreCase(decision)) {
            loan.setStatus(LoanStatus.ACTIVE);
            loan.setApprovedByUser(securityUtils.getCurrentUser());
            loan.setApprovedAt(LocalDateTime.now());
            loan.setStartDate(LocalDate.now());
            loan.setEndDate(LocalDate.now().plusMonths(loan.getTermMonths()));
            loan.setOutstandingBalance(loan.getAmount());

            // Calculate monthly payment: M = P * [r(1+r)^n] / [(1+r)^n - 1]
            BigDecimal monthlyRate = loan.getInterestRate()
                    .divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
            BigDecimal factor = BigDecimal.ONE.add(monthlyRate)
                    .pow(loan.getTermMonths());
            BigDecimal monthlyPayment = loan.getAmount()
                    .multiply(monthlyRate).multiply(factor)
                    .divide(factor.subtract(BigDecimal.ONE), 4, RoundingMode.HALF_UP);
            loan.setMonthlyPayment(monthlyPayment);

            Loan savedLoan = loanRepository.save(loan);

            // Auto-generate repayment schedule
            generateRepaymentSchedule(savedLoan);

            // Send loan status email + notification
            sendLoanNotification(savedLoan, "APPROVED");

            return toLoanResponse(savedLoan);
        } else {
            loan.setStatus(LoanStatus.REJECTED);
            Loan savedLoan = loanRepository.save(loan);

            // Send rejection email + notification
            sendLoanNotification(savedLoan, "REJECTED");

            return toLoanResponse(savedLoan);
        }
    }

    public List<LoanPaymentResponse> getRepaymentSchedule(Long loanId) {
        findOrThrow(loanId); // ensure loan exists
        return loanPaymentRepository.findByLoanIdOrderByPaymentDateDesc(loanId)
                .stream().map(this::toPaymentResponse).collect(Collectors.toList());
    }

    // ── helpers ──────────────────────────────────────────────────────

    private void sendLoanNotification(Loan loan, String status) {
        if (loan.getUser() != null) {
            String name = loan.getUser().getFirstName() + " " + loan.getUser().getLastName();
            emailService.sendLoanStatusEmail(loan.getUser().getEmail(), name, status, loan.getAmount());
            notificationService.createNotification(loan.getUser().getId(), "LOAN",
                    "Your loan application for " + loan.getAmount() + " has been " + status + ".");
        }
    }

    private void generateRepaymentSchedule(Loan loan) {
        BigDecimal remainingBalance = loan.getAmount();
        BigDecimal monthlyRate = loan.getInterestRate()
                .divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        List<LoanPayment> payments = new ArrayList<>();

        for (int i = 1; i <= loan.getTermMonths(); i++) {
            BigDecimal interest = remainingBalance.multiply(monthlyRate)
                    .setScale(4, RoundingMode.HALF_UP);
            BigDecimal principal = loan.getMonthlyPayment().subtract(interest);
            remainingBalance = remainingBalance.subtract(principal);

            payments.add(LoanPayment.builder()
                    .loan(loan)
                    .amount(loan.getMonthlyPayment())
                    .principal(principal)
                    .interest(interest)
                    .dueDate(loan.getStartDate().plusMonths(i))
                    .paymentDate(loan.getStartDate().plusMonths(i))
                    .status(PaymentStatus.PENDING)
                    .build());
        }
        loanPaymentRepository.saveAll(payments);
    }

    private Loan findOrThrow(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + id));
    }

    private LoanResponse toLoanResponse(Loan l) {
        String name = l.getUser() != null
                ? l.getUser().getFirstName() + " " + l.getUser().getLastName() : null;
        return LoanResponse.builder()
                .id(l.getId()).userId(l.getUser() != null ? l.getUser().getId() : null)
                .borrowerName(name)
                .accountId(l.getAccount() != null ? l.getAccount().getId() : null)
                .loanType(l.getLoanType().name())
                .amount(l.getAmount()).interestRate(l.getInterestRate())
                .termMonths(l.getTermMonths()).monthlyPayment(l.getMonthlyPayment())
                .outstandingBalance(l.getOutstandingBalance())
                .status(l.getStatus().name()).purpose(l.getPurpose())
                .startDate(l.getStartDate()).endDate(l.getEndDate())
                .createdAt(l.getCreatedAt())
                .build();
    }

    private LoanPaymentResponse toPaymentResponse(LoanPayment p) {
        return LoanPaymentResponse.builder()
                .id(p.getId()).loanId(p.getLoan().getId())
                .amount(p.getAmount()).principal(p.getPrincipal())
                .interest(p.getInterest())
                .paymentDate(p.getPaymentDate()).dueDate(p.getDueDate())
                .status(p.getStatus().name()).createdAt(p.getCreatedAt())
                .build();
    }
}
