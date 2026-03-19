package com.virtbank.service;

import com.virtbank.entity.TokenBlacklist;
import com.virtbank.repository.TokenBlacklistRepository;
import com.virtbank.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final TokenBlacklistRepository blacklistRepository;
    private final JwtUtils jwtUtils;

    /**
     * Blacklist a token (called on logout).
     */
    public void blacklist(String token) {
        String hash = hashToken(token);
        if (blacklistRepository.existsByTokenHash(hash)) return;

        // Extract expiry from the token itself
        LocalDateTime expiry;
        try {
            long expiryMs = jwtUtils.extractExpiration(token).getTime();
            expiry = Instant.ofEpochMilli(expiryMs).atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception e) {
            expiry = LocalDateTime.now().plusDays(1); // fallback
        }

        blacklistRepository.save(TokenBlacklist.builder()
                .tokenHash(hash)
                .expiry(expiry)
                .build());
        log.info("Token blacklisted (hash: {}…)", hash.substring(0, 8));
    }

    /**
     * Check if a token is blacklisted.
     */
    public boolean isBlacklisted(String token) {
        return blacklistRepository.existsByTokenHash(hashToken(token));
    }

    /**
     * Nightly cleanup — delete expired blacklist entries.
     * Runs every day at 2:00 AM.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredTokens() {
        int deleted = blacklistRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Token blacklist cleanup: {} expired entries removed", deleted);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
