package com.virtbank.service;

import com.virtbank.dto.BusinessMemberResponse;
import com.virtbank.entity.Business;
import com.virtbank.entity.BusinessMember;
import com.virtbank.entity.User;
import com.virtbank.entity.enums.BusinessMemberRole;
import com.virtbank.entity.enums.UserStatus;
import com.virtbank.exception.ResourceNotFoundException;
import com.virtbank.repository.BusinessMemberRepository;
import com.virtbank.repository.BusinessRepository;
import com.virtbank.repository.UserRepository;
import com.virtbank.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusinessMemberService {

    private final BusinessRepository businessRepository;
    private final BusinessMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public BusinessMemberResponse inviteMember(String email, String role) {
        Business business = getOwnBusiness();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        BusinessMember member = BusinessMember.builder()
                .business(business)
                .user(user)
                .role(BusinessMemberRole.valueOf(role.toUpperCase()))
                .status(UserStatus.ACTIVE)
                .build();
        return toResponse(memberRepository.save(member));
    }

    public List<BusinessMemberResponse> getMembers() {
        Business business = getOwnBusiness();
        return memberRepository.findByBusinessId(business.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public void revokeMember(Long memberId) {
        BusinessMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + memberId));
        // Verify this member belongs to the current user's business
        Business business = getOwnBusiness();
        if (!member.getBusiness().getId().equals(business.getId())) {
            throw new com.virtbank.exception.UnauthorizedAccessException("Not your business member");
        }
        member.setStatus(UserStatus.INACTIVE);
        memberRepository.save(member);
    }

    private Business getOwnBusiness() {
        Long userId = securityUtils.getCurrentUserId();
        return businessRepository.findByOwnerId(userId).stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No business found for the current user"));
    }

    private BusinessMemberResponse toResponse(BusinessMember m) {
        return BusinessMemberResponse.builder()
                .id(m.getId()).businessId(m.getBusiness().getId())
                .userId(m.getUser().getId())
                .userName(m.getUser().getFirstName() + " " + m.getUser().getLastName())
                .email(m.getUser().getEmail())
                .role(m.getRole().name()).status(m.getStatus().name())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
