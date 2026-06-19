package com.vehicle.management.infrastructure.persistence.jpa;

import com.vehicle.management.infrastructure.persistence.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaNotificationRepo extends JpaRepository<NotificationEntity, UUID> {
    List<NotificationEntity> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);
    long countByRecipientIdAndReadFalse(UUID recipientId);
}
