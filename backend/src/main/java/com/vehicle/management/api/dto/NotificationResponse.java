package com.vehicle.management.api.dto;

import com.vehicle.management.domain.model.Notification;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String type,
        String title,
        String content,
        boolean read,
        UUID relatedBorrowingId,
        Instant createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getType(), n.getTitle(), n.getContent(),
                n.isRead(), n.getRelatedBorrowingId(), n.getCreatedAt());
    }
}
