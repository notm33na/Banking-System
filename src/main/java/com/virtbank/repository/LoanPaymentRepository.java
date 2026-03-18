package com.virtbank.repository;

import com.virtbank.entity.LoanPayment;
import com.virtbank.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanPaymentRepository extends JpaRepository<LoanPayment, Long> {

    List<LoanPayment> findByLoanId(Long loanId);

    List<LoanPayment> findByLoanIdAndStatus(Long loanId, PaymentStatus status);

    @Query("SELECT lp FROM LoanPayment lp WHERE lp.dueDate < :date AND lp.status = 'PENDING'")
    List<LoanPayment> findOverduePayments(@Param("date") LocalDate date);

    @Query("SELECT lp FROM LoanPayment lp WHERE lp.loan.id = :loanId ORDER BY lp.paymentDate DESC")
    List<LoanPayment> findByLoanIdOrderByPaymentDateDesc(@Param("loanId") Long loanId);
}
