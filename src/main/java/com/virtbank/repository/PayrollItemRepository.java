package com.virtbank.repository;

import com.virtbank.entity.PayrollItem;
import com.virtbank.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PayrollItemRepository extends JpaRepository<PayrollItem, Long> {

    List<PayrollItem> findByPayrollId(Long payrollId);

    List<PayrollItem> findByPayrollIdAndStatus(Long payrollId, PaymentStatus status);

    @Query("SELECT SUM(pi.amount) FROM PayrollItem pi WHERE pi.payroll.id = :payrollId")
    BigDecimal sumAmountByPayrollId(@Param("payrollId") Long payrollId);
}
