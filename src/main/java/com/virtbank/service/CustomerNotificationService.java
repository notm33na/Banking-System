package com.virtbank.service;

import com.virtbank.dto.NotificationResponse;
import com.virtbank.repository.NotificationRepository;
import com.virtbank.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerNotificationService {

    private final NotificationRepository notificationRepository;
    private final SecurityUtils securityUtils;

    public Page<NotificationResponse> getMyNotifications(Pageable pageable) {
        Long userId = securityUtils.getCurrentUserId();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(n -> NotificationResponse.builder()
                        .id(n.getId())
                        .notificationType(n.getNotificationType().name())
                        .subject(n.getSubject())
                        .message(n.getMessage())
                        .isRead(n.getIsRead())
                        .createdAt(n.getCreatedAt())
                        .build());
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            securityUtils.assertOwnership(n.getUser().getId());
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public int markAllAsRead() {
        Long userId = securityUtils.getCurrentUserId();
        return notificationRepository.markAllAsReadByUserId(userId);
    }

    public long getUnreadCount() {
        Long userId = securityUtils.getCurrentUserId();
        return notificationRepository.countUnreadByUserId(userId);
    }
}
