package com.virtbank.repository;

import com.virtbank.entity.Payroll;
import com.virtbank.entity.enums.PayrollStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    List<Payroll> findByBusinessId(Long businessId);

    Page<Payroll> findByBusinessId(Long businessId, Pageable pageable);

    List<Payroll> findByBusinessIdAndStatus(Long businessId, PayrollStatus status);

    @Query("SELECT p FROM Payroll p WHERE p.business.id = :businessId AND p.payPeriodStart >= :start AND p.payPeriodEnd <= :end")
    List<Payroll> findByBusinessIdAndPeriod(
            @Param("businessId") Long businessId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("SELECT p FROM Payroll p WHERE p.business.id = :businessId ORDER BY p.createdAt DESC")
    List<Payroll> findByBusinessIdOrderByCreatedAtDesc(@Param("businessId") Long businessId);
}
