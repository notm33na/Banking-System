package com.virtbank.repository;

import com.virtbank.entity.SupportTicket;
import com.virtbank.entity.enums.TicketPriority;
import com.virtbank.entity.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    Page<SupportTicket> findByUserId(Long userId, Pageable pageable);

    List<SupportTicket> findByStatus(TicketStatus status);

    List<SupportTicket> findByAssignedToUserId(Long assignedToUserId);

    @Query("SELECT t FROM SupportTicket t WHERE t.status IN ('OPEN', 'IN_PROGRESS') AND t.priority = :priority ORDER BY t.createdAt ASC")
    List<SupportTicket> findActiveTicketsByPriority(@Param("priority") TicketPriority priority);

    @Query("SELECT t FROM SupportTicket t WHERE t.assignedToUser IS NULL AND t.status = 'OPEN' ORDER BY t.priority DESC, t.createdAt ASC")
    List<SupportTicket> findUnassignedOpenTickets();

    @Query("SELECT COUNT(t) FROM SupportTicket t WHERE t.user.id = :userId AND t.status IN ('OPEN', 'IN_PROGRESS')")
    long countActiveTicketsByUserId(@Param("userId") Long userId);
}
