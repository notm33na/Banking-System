package com.virtbank.repository;

import com.virtbank.entity.Invoice;
import com.virtbank.entity.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByBusinessId(Long businessId);

    Page<Invoice> findByBusinessId(Long businessId, Pageable pageable);

    List<Invoice> findByBusinessIdAndStatus(Long businessId, InvoiceStatus status);

    @Query("SELECT i FROM Invoice i WHERE i.dueDate < :date AND i.status = 'SENT'")
    List<Invoice> findOverdueInvoices(@Param("date") LocalDate date);

    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.business.id = :businessId AND i.status = :status")
    BigDecimal sumTotalAmountByBusinessAndStatus(@Param("businessId") Long businessId, @Param("status") InvoiceStatus status);

    @Query("SELECT i FROM Invoice i WHERE i.business.id = :businessId ORDER BY i.createdAt DESC")
    List<Invoice> findByBusinessIdOrderByCreatedAtDesc(@Param("businessId") Long businessId);
}
