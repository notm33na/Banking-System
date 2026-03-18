package com.virtbank.repository;

import com.virtbank.entity.BulkTransaction;
import com.virtbank.entity.enums.BulkTransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BulkTransactionRepository extends JpaRepository<BulkTransaction, Long> {

    List<BulkTransaction> findByBusinessId(Long businessId);

    Page<BulkTransaction> findByBusinessId(Long businessId, Pageable pageable);

    List<BulkTransaction> findByBusinessIdAndStatus(Long businessId, BulkTransactionStatus status);

    @Query("SELECT bt FROM BulkTransaction bt WHERE bt.initiatedByUser.id = :userId ORDER BY bt.createdAt DESC")
    List<BulkTransaction> findByInitiatorOrderByDate(@Param("userId") Long userId);

    @Query("SELECT bt FROM BulkTransaction bt WHERE bt.status = 'PENDING' ORDER BY bt.createdAt ASC")
    List<BulkTransaction> findPendingBulkTransactions();
}
