package com.virtbank.repository;

import com.virtbank.entity.TicketMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {

    List<TicketMessage> findByTicketIdOrderByCreatedAtAsc(Long ticketId);

    @Query("SELECT COUNT(m) FROM TicketMessage m WHERE m.ticket.id = :ticketId")
    long countByTicketId(@Param("ticketId") Long ticketId);

    @Query("SELECT m FROM TicketMessage m WHERE m.sender.id = :senderId ORDER BY m.createdAt DESC")
    List<TicketMessage> findBySenderIdOrderByDate(@Param("senderId") Long senderId);
}
