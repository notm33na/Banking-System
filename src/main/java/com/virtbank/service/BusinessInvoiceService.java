package com.virtbank.service;

import com.virtbank.dto.CreateInvoiceRequest;
import com.virtbank.dto.InvoiceResponse;
import com.virtbank.entity.Business;
import com.virtbank.entity.Invoice;
import com.virtbank.entity.InvoiceItem;
import com.virtbank.entity.enums.InvoiceStatus;
import com.virtbank.exception.ResourceNotFoundException;
import com.virtbank.repository.BusinessRepository;
import com.virtbank.repository.InvoiceRepository;
import com.virtbank.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BusinessInvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final BusinessRepository businessRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest req) {
        Business business = getOwnBusiness();

        BigDecimal subtotal = req.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.10)); // 10% tax
        BigDecimal total = subtotal.add(tax);

        Invoice invoice = Invoice.builder()
                .business(business)
                .invoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .customerName(req.getCustomerName())
                .customerEmail(req.getCustomerEmail())
                .dueDate(req.getDueDate())
                .subtotal(subtotal).taxAmount(tax).totalAmount(total)
                .status(InvoiceStatus.DRAFT)
                .notes(req.getNotes())
                .build();

        req.getItems().forEach(item -> {
            InvoiceItem ii = InvoiceItem.builder()
                    .invoice(invoice)
                    .description(item.getDescription())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .amount(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .build();
            invoice.getItems().add(ii);
        });

        return toResponse(invoiceRepository.save(invoice));
    }

    @Transactional
    public InvoiceResponse updateInvoiceStatus(Long invoiceId, String status) {
        Invoice invoice = findOwnInvoice(invoiceId);
        invoice.setStatus(InvoiceStatus.valueOf(status.toUpperCase()));
        return toResponse(invoiceRepository.save(invoice));
    }

    public Page<InvoiceResponse> getInvoices(Pageable pageable) {
        Business business = getOwnBusiness();
        return invoiceRepository.findByBusinessId(business.getId(), pageable).map(this::toResponse);
    }

    public InvoiceResponse getInvoiceById(Long id) {
        return toResponse(findOwnInvoice(id));
    }

    private Invoice findOwnInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + id));
        Business business = getOwnBusiness();
        if (!invoice.getBusiness().getId().equals(business.getId())) {
            throw new com.virtbank.exception.UnauthorizedAccessException("Not your invoice");
        }
        return invoice;
    }

    private Business getOwnBusiness() {
        Long userId = securityUtils.getCurrentUserId();
        return businessRepository.findByOwnerId(userId).stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No business found"));
    }

    private InvoiceResponse toResponse(Invoice i) {
        return InvoiceResponse.builder()
                .id(i.getId()).businessId(i.getBusiness().getId())
                .invoiceNumber(i.getInvoiceNumber())
                .customerName(i.getCustomerName())
                .customerEmail(i.getCustomerEmail())
                .dueDate(i.getDueDate())
                .subtotal(i.getSubtotal()).tax(i.getTaxAmount())
                .total(i.getTotalAmount())
                .status(i.getStatus().name())
                .notes(i.getNotes())
                .createdAt(i.getCreatedAt())
                .build();
    }
}
