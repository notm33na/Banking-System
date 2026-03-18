package com.virtbank.repository;

import com.virtbank.entity.KycDocument;
import com.virtbank.entity.enums.KycStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface KycDocumentRepository extends JpaRepository<KycDocument, Long> {

    List<KycDocument> findByUserId(Long userId);

    List<KycDocument> findByStatus(KycStatus status);

    @Query("SELECT k FROM KycDocument k WHERE k.user.id = :userId AND k.status = :status")
    List<KycDocument> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") KycStatus status);

    @Query("SELECT k FROM KycDocument k WHERE k.expiryDate < :date AND k.status = 'APPROVED'")
    List<KycDocument> findExpiredDocuments(@Param("date") LocalDate date);

    @Query("SELECT k FROM KycDocument k WHERE k.status = 'PENDING' ORDER BY k.createdAt ASC")
    List<KycDocument> findPendingDocumentsOrderByDate();
}
