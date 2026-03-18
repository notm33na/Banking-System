package com.virtbank.service;

import com.virtbank.config.Audited;
import com.virtbank.dto.TicketMessageResponse;
import com.virtbank.dto.TicketResponse;
import com.virtbank.entity.SupportTicket;
import com.virtbank.entity.TicketMessage;
import com.virtbank.entity.User;
import com.virtbank.entity.enums.TicketStatus;
import com.virtbank.exception.ResourceNotFoundException;
import com.virtbank.repository.SupportTicketRepository;
import com.virtbank.repository.TicketMessageRepository;
import com.virtbank.repository.UserRepository;
import com.virtbank.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminTicketService {

    private final SupportTicketRepository ticketRepository;
    private final TicketMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    public Page<TicketResponse> getAllTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable).map(this::toResponse);
    }

    @Audited(action = "ASSIGN_TICKET", entityType = "SupportTicket")
    @Transactional
    public TicketResponse assignTicket(Long ticketId, Long assigneeId) {
        SupportTicket ticket = findOrThrow(ticketId);
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id: " + assigneeId));
        ticket.setAssignedToUser(assignee);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        return toResponse(ticketRepository.save(ticket));
    }

    @Audited(action = "UPDATE_TICKET_STATUS", entityType = "SupportTicket")
    @Transactional
    public TicketResponse updateTicketStatus(Long ticketId, String status) {
        SupportTicket ticket = findOrThrow(ticketId);
        TicketStatus newStatus = TicketStatus.valueOf(status.toUpperCase());
        ticket.setStatus(newStatus);
        if (newStatus == TicketStatus.CLOSED || newStatus == TicketStatus.RESOLVED) {
            ticket.setClosedAt(LocalDateTime.now());
        }
        return toResponse(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketMessageResponse replyToTicket(Long ticketId, String messageText) {
        SupportTicket ticket = findOrThrow(ticketId);
        User sender = securityUtils.getCurrentUser();

        TicketMessage msg = TicketMessage.builder()
                .ticket(ticket)
                .sender(sender)
                .message(messageText)
                .build();
        TicketMessage saved = messageRepository.save(msg);
        return toMessageResponse(saved);
    }

    public List<TicketMessageResponse> getTicketMessages(Long ticketId) {
        findOrThrow(ticketId);
        return messageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream().map(this::toMessageResponse).collect(Collectors.toList());
    }

    // ── helpers ──────────────────────────────────────────────────────

    private SupportTicket findOrThrow(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
    }

    private TicketResponse toResponse(SupportTicket t) {
        String userName = t.getUser() != null
                ? t.getUser().getFirstName() + " " + t.getUser().getLastName() : null;
        String assigneeName = t.getAssignedToUser() != null
                ? t.getAssignedToUser().getFirstName() + " " + t.getAssignedToUser().getLastName() : null;
        return TicketResponse.builder()
                .id(t.getId())
                .userId(t.getUser() != null ? t.getUser().getId() : null)
                .userName(userName).subject(t.getSubject())
                .category(t.getCategory().name())
                .priority(t.getPriority().name())
                .status(t.getStatus().name())
                .assignedTo(t.getAssignedToUser() != null ? t.getAssignedToUser().getId() : null)
                .assignedToName(assigneeName)
                .createdAt(t.getCreatedAt()).updatedAt(t.getUpdatedAt())
                .closedAt(t.getClosedAt())
                .build();
    }

    private TicketMessageResponse toMessageResponse(TicketMessage m) {
        return TicketMessageResponse.builder()
                .id(m.getId()).ticketId(m.getTicket().getId())
                .senderId(m.getSender().getId())
                .senderName(m.getSender().getFirstName() + " " + m.getSender().getLastName())
                .message(m.getMessage()).createdAt(m.getCreatedAt())
                .build();
    }
}
