package com.virtbank.service;

import com.virtbank.dto.TicketMessageResponse;
import com.virtbank.dto.TicketResponse;
import com.virtbank.entity.SupportTicket;
import com.virtbank.entity.TicketMessage;
import com.virtbank.entity.User;
import com.virtbank.entity.enums.TicketCategory;
import com.virtbank.entity.enums.TicketPriority;
import com.virtbank.entity.enums.TicketStatus;
import com.virtbank.exception.ResourceNotFoundException;
import com.virtbank.repository.SupportTicketRepository;
import com.virtbank.repository.TicketMessageRepository;
import com.virtbank.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerTicketService {

    private final SupportTicketRepository ticketRepository;
    private final TicketMessageRepository messageRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public TicketResponse createTicket(String subject, String category, String priority, String firstMessage) {
        User user = securityUtils.getCurrentUser();

        SupportTicket ticket = SupportTicket.builder()
                .user(user).subject(subject)
                .category(TicketCategory.valueOf(category.toUpperCase()))
                .priority(TicketPriority.valueOf(priority.toUpperCase()))
                .status(TicketStatus.OPEN)
                .build();
        SupportTicket saved = ticketRepository.save(ticket);

        // Add the first message
        messageRepository.save(TicketMessage.builder()
                .ticket(saved).sender(user).message(firstMessage).build());

        return toResponse(saved);
    }

    public Page<TicketResponse> getMyTickets(Pageable pageable) {
        Long userId = securityUtils.getCurrentUserId();
        return ticketRepository.findByUserId(userId, pageable).map(this::toResponse);
    }

    public List<TicketMessageResponse> getTicketMessages(Long ticketId) {
        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));
        securityUtils.assertOwnership(ticket.getUser().getId());

        return messageRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream().map(this::toMessageResponse).collect(Collectors.toList());
    }

    private TicketResponse toResponse(SupportTicket t) {
        return TicketResponse.builder()
                .id(t.getId()).userId(t.getUser().getId())
                .userName(t.getUser().getFirstName() + " " + t.getUser().getLastName())
                .subject(t.getSubject())
                .category(t.getCategory().name()).priority(t.getPriority().name())
                .status(t.getStatus().name())
                .createdAt(t.getCreatedAt()).updatedAt(t.getUpdatedAt())
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
