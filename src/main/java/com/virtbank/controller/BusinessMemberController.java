package com.virtbank.controller;

import com.virtbank.dto.ApiResponse;
import com.virtbank.dto.BusinessMemberResponse;
import com.virtbank.dto.InviteMemberRequest;
import com.virtbank.service.BusinessMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/business/members")
@PreAuthorize("hasRole('BUSINESS')")
@RequiredArgsConstructor
public class BusinessMemberController {

    private final BusinessMemberService businessMemberService;

    @PostMapping("/invite")
    public ResponseEntity<ApiResponse<BusinessMemberResponse>> inviteMember(
            @Valid @RequestBody InviteMemberRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Member invited",
                businessMemberService.inviteMember(request.getEmail(), request.getRole())));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BusinessMemberResponse>>> getMembers() {
        return ResponseEntity.ok(ApiResponse.success(businessMemberService.getMembers()));
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<ApiResponse<Void>> revokeMember(@PathVariable Long memberId) {
        businessMemberService.revokeMember(memberId);
        return ResponseEntity.ok(ApiResponse.success("Member access revoked", null));
    }
}
