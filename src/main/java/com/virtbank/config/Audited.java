package com.virtbank.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation to mark service methods for automatic audit logging.
 * The AuditAspect AOP advice will capture the action and write an AuditLog entry.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {

    /** A human-readable description of the action, e.g. "UPDATE_USER_STATUS". */
    String action();

    /** The entity type being acted upon, e.g. "User", "Loan". */
    String entityType() default "";
}
