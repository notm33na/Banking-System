package com.virtbank.service;

import com.virtbank.config.AuditAspect;
import com.virtbank.config.Audited;
import com.virtbank.entity.AuditLog;
import com.virtbank.entity.User;
import com.virtbank.repository.AuditLogRepository;
import com.virtbank.util.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock private AuditLogRepository auditLogRepository;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks private AuditAspect auditAspect;

    @Test
    void auditAdvice_savesLogWithCorrectFields() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Audited audited = mock(Audited.class);

        when(audited.action()).thenReturn("LOAN_DECISION");
        when(audited.entityType()).thenReturn("Loan");
        when(joinPoint.proceed()).thenReturn("result-value");
        when(joinPoint.getArgs()).thenReturn(new Object[]{42L, "APPROVED"});

        User admin = User.builder().id(1L).email("admin@test.com").build();
        when(securityUtils.getCurrentUser()).thenReturn(admin);
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        Object result = auditAspect.auditMethod(joinPoint, audited);

        // Verify method return value is not altered
        assertEquals("result-value", result);

        // Verify audit log saved
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog log = captor.getValue();
        assertEquals("LOAN_DECISION", log.getAction());
        assertEquals("Loan", log.getEntityType());
        assertEquals(42L, log.getEntityId());
        assertEquals(admin, log.getUser());
    }

    @Test
    void auditAdvice_doesNotAlterReturnValue() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Audited audited = mock(Audited.class);

        when(audited.action()).thenReturn("TEST_ACTION");
        when(audited.entityType()).thenReturn("");
        when(joinPoint.proceed()).thenReturn("expected-return");
        when(joinPoint.getArgs()).thenReturn(new Object[]{});

        Object result = auditAspect.auditMethod(joinPoint, audited);

        assertEquals("expected-return", result);
        verify(joinPoint).proceed();
    }
}
