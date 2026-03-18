package com.virtbank.config;

import com.virtbank.entity.AuditLog;
import com.virtbank.repository.AuditLogRepository;
import com.virtbank.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditLogRepository auditLogRepository;
    private final SecurityUtils securityUtils;

    @Around("@annotation(audited)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {

        // Execute the actual method
        Object result = joinPoint.proceed();

        // Build audit log entry asynchronously after method succeeds
        try {
            AuditLog.AuditLogBuilder logBuilder = AuditLog.builder()
                    .action(audited.action())
                    .entityType(audited.entityType().isEmpty() ? null : audited.entityType());

            // Try to set the user (may fail for unauthenticated calls)
            try {
                logBuilder.user(securityUtils.getCurrentUser());
            } catch (Exception ignored) {
                // No authenticated user — leave user null
            }

            // Try to extract IP and user-agent from request
            try {
                ServletRequestAttributes attrs =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    HttpServletRequest request = attrs.getRequest();
                    logBuilder.ipAddress(request.getRemoteAddr());
                    logBuilder.userAgent(request.getHeader("User-Agent"));
                }
            } catch (Exception ignored) {
            }

            // Try to extract entity ID from the first Long argument
            for (Object arg : joinPoint.getArgs()) {
                if (arg instanceof Long id) {
                    logBuilder.entityId(id);
                    break;
                }
            }

            auditLogRepository.save(logBuilder.build());
        } catch (Exception e) {
            logger.error("Failed to write audit log: {}", e.getMessage());
        }

        return result;
    }
}
