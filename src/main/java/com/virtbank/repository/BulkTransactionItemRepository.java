package com.virtbank.repository;

import com.virtbank.entity.BulkTransactionItem;
import com.virtbank.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BulkTransactionItemRepository extends JpaRepository<BulkTransactionItem, Long> {

    List<BulkTransactionItem> findByBulkTransactionId(Long bulkTransactionId);

    List<BulkTransactionItem> findByBulkTransactionIdAndStatus(Long bulkTransactionId, PaymentStatus status);

    @Query("SELECT COUNT(bti) FROM BulkTransactionItem bti WHERE bti.bulkTransaction.id = :bulkTxId AND bti.status = :status")
    long countByBulkTransactionIdAndStatus(@Param("bulkTxId") Long bulkTxId, @Param("status") PaymentStatus status);
}
