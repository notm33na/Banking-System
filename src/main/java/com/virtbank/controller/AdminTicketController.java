package com.virtbank.controller;

import com.virtbank.dto.*;
import com.virtbank.service.AdminTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/tickets")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminTicketController {

    private final AdminTicketService adminTicketService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> getAllTickets(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(adminTicketService.getAllTickets(pageable)));
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<TicketResponse>> assignTicket(
            @PathVariable Long id, @Valid @RequestBody AssignTicketRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Ticket assigned",
                adminTicketService.assignTicket(id, request.getAssigneeId())));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TicketResponse>> updateTicketStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateTicketStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Ticket status updated",
                adminTicketService.updateTicketStatus(id, request.getStatus())));
    }

    @PostMapping("/{id}/reply")
    public ResponseEntity<ApiResponse<TicketMessageResponse>> replyToTicket(
            @PathVariable Long id, @Valid @RequestBody TicketReplyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Reply sent",
                adminTicketService.replyToTicket(id, request.getMessage())));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<List<TicketMessageResponse>>> getTicketMessages(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminTicketService.getTicketMessages(id)));
    }
}
