package com.virtbank.service;

import com.virtbank.config.Audited;
import com.virtbank.dto.KycResponse;
import com.virtbank.entity.KycDocument;
import com.virtbank.entity.enums.KycStatus;
import com.virtbank.exception.ResourceNotFoundException;
import com.virtbank.repository.KycDocumentRepository;
import com.virtbank.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminKycService {

    private final KycDocumentRepository kycDocumentRepository;
    private final SecurityUtils securityUtils;

    public Page<KycResponse> getAllKycDocuments(Pageable pageable) {
        return kycDocumentRepository.findAll(pageable).map(this::toResponse);
    }

    public List<KycResponse> getPendingKycDocuments() {
        return kycDocumentRepository.findByStatus(KycStatus.PENDING)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<KycResponse> getExpiredKycDocuments() {
        return kycDocumentRepository.findExpiredDocuments(LocalDate.now())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Audited(action = "KYC_DECISION", entityType = "KycDocument")
    @Transactional
    public KycResponse updateKycStatus(Long id, String status, String rejectionReason) {
        KycDocument doc = kycDocumentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KYC document not found with id: " + id));
        doc.setStatus(KycStatus.valueOf(status.toUpperCase()));
        doc.setRejectionReason(rejectionReason);
        doc.setReviewedByUser(securityUtils.getCurrentUser());
        doc.setReviewedAt(LocalDateTime.now());
        return toResponse(kycDocumentRepository.save(doc));
    }

    private KycResponse toResponse(KycDocument k) {
        String userName = k.getUser() != null
                ? k.getUser().getFirstName() + " " + k.getUser().getLastName() : null;
        return KycResponse.builder()
                .id(k.getId()).userId(k.getUser() != null ? k.getUser().getId() : null)
                .userName(userName)
                .documentType(k.getDocumentType().name())
                .documentUrl(k.getDocumentUrl())
                .documentNumber(k.getDocumentNumber())
                .status(k.getStatus().name())
                .rejectionReason(k.getRejectionReason())
                .reviewedBy(k.getReviewedByUser() != null ? k.getReviewedByUser().getId() : null)
                .reviewedAt(k.getReviewedAt())
                .expiryDate(k.getExpiryDate())
                .createdAt(k.getCreatedAt())
                .build();
    }
}
