package com.virtbank.service;

import com.virtbank.dto.DocumentResponse;
import com.virtbank.dto.KycResponse;
import com.virtbank.entity.Document;
import com.virtbank.entity.KycDocument;
import com.virtbank.entity.User;
import com.virtbank.entity.enums.DocumentType;
import com.virtbank.entity.enums.KycStatus;
import com.virtbank.exception.ResourceNotFoundException;
import com.virtbank.repository.DocumentRepository;
import com.virtbank.repository.KycDocumentRepository;
import com.virtbank.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerDocumentService {

    private static final String UPLOAD_DIR = "uploads/documents/";

    private final DocumentRepository documentRepository;
    private final KycDocumentRepository kycDocumentRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public DocumentResponse uploadDocument(MultipartFile file, String documentType) throws IOException {
        User user = securityUtils.getCurrentUser();

        // Save file to disk
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(UPLOAD_DIR);
        Files.createDirectories(uploadPath);
        Path filePath = uploadPath.resolve(fileName);
        file.transferTo(filePath.toFile());

        Document doc = Document.builder()
                .user(user)
                .fileName(file.getOriginalFilename())
                .documentType(documentType)
                .fileUrl(filePath.toString())
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .build();
        return toDocResponse(documentRepository.save(doc));
    }

    @Transactional
    public KycResponse uploadKycDocument(MultipartFile file, String documentType, String documentNumber) throws IOException {
        User user = securityUtils.getCurrentUser();

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(UPLOAD_DIR + "kyc/");
        Files.createDirectories(uploadPath);
        Path filePath = uploadPath.resolve(fileName);
        file.transferTo(filePath.toFile());

        KycDocument kyc = KycDocument.builder()
                .user(user)
                .documentType(DocumentType.valueOf(documentType.toUpperCase()))
                .documentUrl(filePath.toString())
                .documentNumber(documentNumber)
                .status(KycStatus.PENDING)
                .build();
        return toKycResponse(kycDocumentRepository.save(kyc));
    }

    public List<DocumentResponse> getMyDocuments() {
        Long userId = securityUtils.getCurrentUserId();
        return documentRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDocResponse).collect(Collectors.toList());
    }

    public List<KycResponse> getMyKycStatus() {
        Long userId = securityUtils.getCurrentUserId();
        return kycDocumentRepository.findByUserId(userId)
                .stream().map(this::toKycResponse).collect(Collectors.toList());
    }

    public DocumentResponse getDocumentById(Long id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));
        securityUtils.assertOwnership(doc.getUser().getId());
        return toDocResponse(doc);
    }

    private DocumentResponse toDocResponse(Document d) {
        return DocumentResponse.builder()
                .id(d.getId()).userId(d.getUser().getId())
                .fileName(d.getFileName()).documentType(d.getDocumentType())
                .fileUrl(d.getFileUrl()).fileSize(d.getFileSize())
                .mimeType(d.getMimeType()).createdAt(d.getCreatedAt())
                .build();
    }

    private KycResponse toKycResponse(KycDocument k) {
        return KycResponse.builder()
                .id(k.getId()).userId(k.getUser().getId())
                .userName(k.getUser().getFirstName() + " " + k.getUser().getLastName())
                .documentType(k.getDocumentType().name())
                .documentUrl(k.getDocumentUrl())
                .documentNumber(k.getDocumentNumber())
                .status(k.getStatus().name())
                .createdAt(k.getCreatedAt())
                .build();
    }
}
