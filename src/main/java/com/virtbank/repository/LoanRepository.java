package com.virtbank.repository;

import com.virtbank.entity.Loan;
import com.virtbank.entity.enums.LoanStatus;
import com.virtbank.entity.enums.LoanType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByUserId(Long userId);

    List<Loan> findByStatus(LoanStatus status);

    List<Loan> findByUserIdAndStatus(Long userId, LoanStatus status);

    @Query("SELECT l FROM Loan l WHERE l.loanType = :type AND l.status = :status")
    List<Loan> findByTypeAndStatus(@Param("type") LoanType type, @Param("status") LoanStatus status);

    @Query("SELECT SUM(l.outstandingBalance) FROM Loan l WHERE l.user.id = :userId AND l.status = 'ACTIVE'")
    BigDecimal getTotalOutstandingByUserId(@Param("userId") Long userId);

    @Query("SELECT l FROM Loan l WHERE l.status = 'PENDING' ORDER BY l.createdAt ASC")
    List<Loan> findPendingLoansOrderByDate();
}
