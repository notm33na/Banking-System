package com.virtbank.service;

import com.virtbank.entity.Notification;
import com.virtbank.entity.User;
import com.virtbank.entity.enums.NotificationType;
import com.virtbank.repository.NotificationRepository;
import com.virtbank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // ── SSE emitter registry ──────────────────────────────────────────
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(300_000L); // 5 min timeout
        emitters.put(userId, emitter);
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));
        return emitter;
    }

    // ── Create + push notification ────────────────────────────────────
    @Transactional
    public Notification createNotification(Long userId, String type, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        NotificationType notifType;
        try {
            notifType = NotificationType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            notifType = NotificationType.GENERAL;
        }

        Notification notification = Notification.builder()
                .user(user)
                .notificationType(notifType)
                .subject(type)
                .message(message)
                .isRead(false)
                .build();
        Notification saved = notificationRepository.save(notification);

        // Push to SSE if user is connected
        pushToUser(userId, saved);
        return saved;
    }

    private void pushToUser(Long userId, Notification notification) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(Map.of(
                                "id", notification.getId(),
                                "type", notification.getNotificationType().name(),
                                "subject", notification.getSubject() != null ? notification.getSubject() : "",
                                "message", notification.getMessage(),
                                "isRead", false,
                                "createdAt", notification.getCreatedAt() != null
                                        ? notification.getCreatedAt().toString() : ""
                        )));
            } catch (IOException e) {
                emitters.remove(userId);
                log.warn("Failed to push SSE to user {}", userId);
            }
        }
    }
}
