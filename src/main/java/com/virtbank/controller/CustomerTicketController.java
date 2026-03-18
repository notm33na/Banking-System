package com.virtbank.controller;

import com.virtbank.dto.*;
import com.virtbank.service.CustomerTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/tickets")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
public class CustomerTicketController {

    private final CustomerTicketService customerTicketService;

    @PostMapping
    public ResponseEntity<ApiResponse<TicketResponse>> createTicket(
            @Valid @RequestBody CreateTicketRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Ticket created",
                customerTicketService.createTicket(
                        request.getSubject(), request.getCategory(),
                        request.getPriority(), request.getMessage())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> getMyTickets(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(customerTicketService.getMyTickets(pageable)));
    }

    @GetMapping("/{ticketId}/messages")
    public ResponseEntity<ApiResponse<List<TicketMessageResponse>>> getTicketMessages(
            @PathVariable Long ticketId) {
        return ResponseEntity.ok(ApiResponse.success(customerTicketService.getTicketMessages(ticketId)));
    }
}
