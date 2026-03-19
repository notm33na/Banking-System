package com.virtbank.service;

import com.virtbank.dto.CreatePayrollRequest;
import com.virtbank.dto.PayrollResponse;
import com.virtbank.entity.*;
import com.virtbank.entity.enums.PayrollItemStatus;
import com.virtbank.entity.enums.PayrollStatus;
import com.virtbank.exception.InsufficientFundsException;
import com.virtbank.exception.ResourceNotFoundException;
import com.virtbank.repository.AccountRepository;
import com.virtbank.repository.BusinessRepository;
import com.virtbank.repository.PayrollRepository;
import com.virtbank.service.EmailService;
import com.virtbank.service.NotificationService;
import com.virtbank.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BusinessPayrollService {

    private final PayrollRepository payrollRepository;
    private final BusinessRepository businessRepository;
    private final AccountRepository accountRepository;
    private final SecurityUtils securityUtils;
    private final EmailService emailService;
    private final NotificationService notificationService;

    @Transactional
    public PayrollResponse createPayroll(CreatePayrollRequest req) {
        Business business = getOwnBusiness();
        User creator = securityUtils.getCurrentUser();

        BigDecimal totalAmount = req.getItems().stream()
                .map(i -> i.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);

        Payroll payroll = Payroll.builder()
                .business(business).createdByUser(creator)
                .payPeriodStart(req.getPayPeriodStart())
                .payPeriodEnd(req.getPayPeriodEnd())
                .totalAmount(totalAmount)
                .status(PayrollStatus.DRAFT)
                .build();

        req.getItems().forEach(item -> {
            PayrollItem pi = PayrollItem.builder()
                    .payroll(payroll)
                    .employeeName(item.getEmployeeName())
                    .employeeAccount(item.getEmployeeAccount())
                    .amount(item.getAmount())
                    .status(PayrollItemStatus.PENDING)
                    .build();
            payroll.getItems().add(pi);
        });

        return toResponse(payrollRepository.save(payroll));
    }

    @Transactional
    public PayrollResponse runPayroll(Long payrollId) {
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll not found: " + payrollId));

        // Verify ownership
        Business business = getOwnBusiness();
        if (!payroll.getBusiness().getId().equals(business.getId())) {
            throw new com.virtbank.exception.UnauthorizedAccessException("Not your payroll");
        }

        // Mark as processed
        payroll.setStatus(PayrollStatus.PROCESSED);
        payroll.setProcessedAt(LocalDateTime.now());
        payroll.getItems().forEach(item -> {
            item.setStatus(PayrollItemStatus.COMPLETED);

            // Send payroll confirmation to each employee
            String cycle = payroll.getPayPeriodStart() + " – " + payroll.getPayPeriodEnd();
            emailService.sendPayrollConfirmation(
                    item.getEmployeeAccount(), // used as email placeholder — in production map to user email
                    item.getEmployeeName(),
                    item.getAmount(),
                    cycle);
        });

        // Notify the business owner
        Long ownerId = payroll.getBusiness().getOwner().getId();
        notificationService.createNotification(ownerId, "PAYROLL",
                "Payroll processed: " + payroll.getTotalAmount() + " disbursed to " +
                payroll.getItems().size() + " employees.");

        return toResponse(payrollRepository.save(payroll));
    }

    public Page<PayrollResponse> getPayrollHistory(Pageable pageable) {
        Business business = getOwnBusiness();
        return payrollRepository.findByBusinessId(business.getId(), pageable).map(this::toResponse);
    }

    private Business getOwnBusiness() {
        Long userId = securityUtils.getCurrentUserId();
        return businessRepository.findByOwnerId(userId).stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No business found"));
    }

    private PayrollResponse toResponse(Payroll p) {
        return PayrollResponse.builder()
                .id(p.getId()).businessId(p.getBusiness().getId())
                .payPeriodStart(p.getPayPeriodStart())
                .payPeriodEnd(p.getPayPeriodEnd())
                .totalAmount(p.getTotalAmount())
                .status(p.getStatus().name())
                .itemCount(p.getItems().size())
                .createdAt(p.getCreatedAt())
                .processedAt(p.getProcessedAt())
                .build();
    }
}
