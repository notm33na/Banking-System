package com.virtbank.controller;

import com.virtbank.security.JwtUtils;
import com.virtbank.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class SseController {

    private final NotificationService notificationService;
    private final JwtUtils jwtUtils;

    /**
     * SSE stream endpoint.
     * EventSource cannot send custom headers, so the JWT is passed as a query param.
     * Example: /api/notifications/stream?token=eyJ...
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam("token") String token) {
        // Validate token and extract userId
        if (!jwtUtils.validateToken(token)) {
            throw new RuntimeException("Invalid token");
        }
        Long userId = jwtUtils.extractUserId(token);
        return notificationService.subscribe(userId);
    }
}
