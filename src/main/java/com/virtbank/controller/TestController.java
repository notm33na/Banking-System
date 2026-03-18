package com.virtbank.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Dummy endpoints to verify authentication and role-based access control.
 * Remove or replace in later phases.
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    // ── Public — no auth required ────────────────────────────────────
    @GetMapping("/public")
    public ResponseEntity<Map<String, String>> publicEndpoint() {
        return ResponseEntity.ok(Map.of(
                "message", "This is a PUBLIC endpoint — no authentication needed."));
    }

    // ── Customer only ────────────────────────────────────────────────
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/customer")
    public ResponseEntity<Map<String, String>> customerEndpoint() {
        return ResponseEntity.ok(Map.of(
                "message", "Hello CUSTOMER! You have ROLE_CUSTOMER access."));
    }

    // ── Admin only ───────────────────────────────────────────────────
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<Map<String, String>> adminEndpoint() {
        return ResponseEntity.ok(Map.of(
                "message", "Hello ADMIN! You have ROLE_ADMIN access."));
    }

    // ── Business only ────────────────────────────────────────────────
    @PreAuthorize("hasRole('BUSINESS')")
    @GetMapping("/business")
    public ResponseEntity<Map<String, String>> businessEndpoint() {
        return ResponseEntity.ok(Map.of(
                "message", "Hello BUSINESS! You have ROLE_BUSINESS access."));
    }
}
