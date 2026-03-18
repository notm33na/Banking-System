package com.virtbank.service;

import com.virtbank.dto.LoanApplicationRequest;
import com.virtbank.dto.LoanPaymentResponse;
import com.virtbank.dto.LoanResponse;
import com.virtbank.entity.Account;
import com.virtbank.entity.Loan;
import com.virtbank.entity.User;
import com.virtbank.entity.enums.LoanStatus;
import com.virtbank.entity.enums.LoanType;
import com.virtbank.exception.ResourceNotFoundException;
import com.virtbank.repository.AccountRepository;
import com.virtbank.repository.LoanPaymentRepository;
import com.virtbank.repository.LoanRepository;
import com.virtbank.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerLoanService {

    private final LoanRepository loanRepository;
    private final LoanPaymentRepository loanPaymentRepository;
    private final AccountRepository accountRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public LoanResponse applyForLoan(LoanApplicationRequest req) {
        User user = securityUtils.getCurrentUser();
        Account account = accountRepository.findById(req.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + req.getAccountId()));
        securityUtils.assertOwnership(account.getUser().getId());

        Loan loan = Loan.builder()
                .user(user).account(account)
                .loanType(LoanType.valueOf(req.getLoanType().toUpperCase()))
                .amount(req.getAmount()).interestRate(req.getInterestRate())
                .termMonths(req.getTermMonths()).purpose(req.getPurpose())
                .status(LoanStatus.PENDING)
                .build();
        return toLoanResponse(loanRepository.save(loan));
    }

    public List<LoanResponse> getMyLoans() {
        Long userId = securityUtils.getCurrentUserId();
        return loanRepository.findByUserId(userId)
                .stream().map(this::toLoanResponse).collect(Collectors.toList());
    }

    public List<LoanPaymentResponse> getMyRepaymentSchedule(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + loanId));
        securityUtils.assertOwnership(loan.getUser().getId());

        return loanPaymentRepository.findByLoanIdOrderByPaymentDateDesc(loanId)
                .stream().map(p -> LoanPaymentResponse.builder()
                        .id(p.getId()).loanId(p.getLoan().getId())
                        .amount(p.getAmount()).principal(p.getPrincipal())
                        .interest(p.getInterest())
                        .paymentDate(p.getPaymentDate()).dueDate(p.getDueDate())
                        .status(p.getStatus().name()).createdAt(p.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private LoanResponse toLoanResponse(Loan l) {
        return LoanResponse.builder()
                .id(l.getId()).userId(l.getUser().getId())
                .borrowerName(l.getUser().getFirstName() + " " + l.getUser().getLastName())
                .accountId(l.getAccount().getId())
                .loanType(l.getLoanType().name())
                .amount(l.getAmount()).interestRate(l.getInterestRate())
                .termMonths(l.getTermMonths()).monthlyPayment(l.getMonthlyPayment())
                .outstandingBalance(l.getOutstandingBalance())
                .status(l.getStatus().name()).purpose(l.getPurpose())
                .startDate(l.getStartDate()).endDate(l.getEndDate())
                .createdAt(l.getCreatedAt())
                .build();
    }
}
