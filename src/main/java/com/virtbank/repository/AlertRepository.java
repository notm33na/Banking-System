package com.virtbank.repository;

import com.virtbank.entity.Alert;
import com.virtbank.entity.enums.AlertType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    Page<Alert> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Alert> findByUserIdAndIsReadFalse(Long userId);

    @Query("SELECT a FROM Alert a WHERE a.user.id = :userId AND a.alertType = :type ORDER BY a.createdAt DESC")
    List<Alert> findByUserIdAndAlertType(@Param("userId") Long userId, @Param("type") AlertType type);

    @Query("SELECT COUNT(a) FROM Alert a WHERE a.user.id = :userId AND a.isRead = false")
    long countUnreadByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Alert a SET a.isRead = true WHERE a.user.id = :userId AND a.isRead = false")
    int markAllAsReadByUserId(@Param("userId") Long userId);
}
