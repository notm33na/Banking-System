package com.virtbank.service;

import com.virtbank.dto.CreatePayrollRequest;
import com.virtbank.dto.PayrollResponse;
import com.virtbank.entity.*;
import com.virtbank.entity.enums.PayrollStatus;
import com.virtbank.exception.ResourceNotFoundException;
import com.virtbank.repository.AccountRepository;
import com.virtbank.repository.BusinessRepository;
import com.virtbank.repository.PayrollRepository;
import com.virtbank.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayrollServiceTest {

    @Mock private PayrollRepository payrollRepository;
    @Mock private BusinessRepository businessRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private SecurityUtils securityUtils;
    @Mock private EmailService emailService;
    @Mock private NotificationService notificationService;

    @InjectMocks private BusinessPayrollService payrollService;

    private User owner;
    private Business business;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).firstName("Boss").lastName("Man")
                .email("boss@test.com").build();
        business = Business.builder().id(1L).owner(owner)
                .companyName("TestCorp").build();
    }

    @Test
    void runPayroll_success_processesItemsAndSendsEmails() {
        PayrollItem item1 = PayrollItem.builder().employeeName("Alice")
                .employeeAccount("alice@test.com").amount(new BigDecimal("3000")).build();
        PayrollItem item2 = PayrollItem.builder().employeeName("Bob")
                .employeeAccount("bob@test.com").amount(new BigDecimal("4000")).build();

        Payroll payroll = Payroll.builder().id(1L).business(business)
                .payPeriodStart("2025-01-01").payPeriodEnd("2025-01-31")
                .totalAmount(new BigDecimal("7000")).status(PayrollStatus.DRAFT)
                .items(new ArrayList<>(List.of(item1, item2))).build();

        when(payrollRepository.findById(1L)).thenReturn(Optional.of(payroll));
        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(businessRepository.findByOwnerId(1L)).thenReturn(List.of(business));
        when(payrollRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PayrollResponse response = payrollService.runPayroll(1L);

        assertEquals("PROCESSED", response.getStatus());

        // Verify email sent to each employee
        verify(emailService).sendPayrollConfirmation(eq("alice@test.com"),
                eq("Alice"), eq(new BigDecimal("3000")), anyString());
        verify(emailService).sendPayrollConfirmation(eq("bob@test.com"),
                eq("Bob"), eq(new BigDecimal("4000")), anyString());

        // Verify owner notification
        verify(notificationService).createNotification(eq(1L), eq("PAYROLL"), anyString());
    }

    @Test
    void runPayroll_notFound_throwsException() {
        when(payrollRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> payrollService.runPayroll(999L));
        verify(emailService, never()).sendPayrollConfirmation(any(), any(), any(), any());
    }

    @Test
    void createPayroll_zeroItems_createsEmptyPayroll() {
        CreatePayrollRequest req = new CreatePayrollRequest();
        req.setPayPeriodStart("2025-01-01");
        req.setPayPeriodEnd("2025-01-31");
        req.setItems(List.of()); // zero employees

        when(securityUtils.getCurrentUserId()).thenReturn(1L);
        when(securityUtils.getCurrentUser()).thenReturn(owner);
        when(businessRepository.findByOwnerId(1L)).thenReturn(List.of(business));
        when(payrollRepository.save(any())).thenAnswer(inv -> {
            Payroll p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        PayrollResponse response = payrollService.createPayroll(req);

        assertNotNull(response);
        assertEquals(0, response.getItemCount());
        verify(emailService, never()).sendPayrollConfirmation(any(), any(), any(), any());
    }
}
