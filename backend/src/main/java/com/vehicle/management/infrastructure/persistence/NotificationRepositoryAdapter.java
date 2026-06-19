package com.vehicle.management.infrastructure.persistence;

import com.vehicle.management.domain.model.Notification;
import com.vehicle.management.infrastructure.persistence.entity.NotificationEntity;
import com.vehicle.management.infrastructure.persistence.jpa.JpaNotificationRepo;
import com.vehicle.management.repository.INotificationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Adapter（Ch14）：將 JPA NotificationEntity 轉換為 Domain Notification。 */
@Repository
public class NotificationRepositoryAdapter implements INotificationRepository {

    private final JpaNotificationRepo jpa;

    public NotificationRepositoryAdapter(JpaNotificationRepo jpa) {
        this.jpa = jpa;
    }

    @Override
    public Notification save(Notification notification) {
        return toDomain(jpa.save(toEntity(notification)));
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Notification> findByRecipient(UUID recipientId) {
        return jpa.findByRecipientIdOrderByCreatedAtDesc(recipientId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public long countUnread(UUID recipientId) {
        return jpa.countByRecipientIdAndReadFalse(recipientId);
    }

    private Notification toDomain(NotificationEntity e) {
        return new Notification(
                e.getId(), e.getRecipientId(), e.getType(), e.getTitle(),
                e.getContent(), e.isRead(), e.getRelatedBorrowingId(), e.getCreatedAt());
    }

    private NotificationEntity toEntity(Notification n) {
        NotificationEntity e = new NotificationEntity();
        e.setId(n.getId());
        e.setRecipientId(n.getRecipientId());
        e.setType(n.getType());
        e.setTitle(n.getTitle());
        e.setContent(n.getContent());
        e.setRead(n.isRead());
        e.setRelatedBorrowingId(n.getRelatedBorrowingId());
        e.setCreatedAt(n.getCreatedAt());
        return e;
    }
}
