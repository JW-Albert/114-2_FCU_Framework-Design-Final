package com.vehicle.management.service;

import com.vehicle.management.domain.model.Notification;
import com.vehicle.management.repository.INotificationRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 站內通知（收件夾）業務邏輯服務。
 *
 * <p>負責通知的建立、查詢、未讀計數與已讀標記。
 * 通知的「建立」來源主要是 {@link com.vehicle.management.domain.observer.InboxNotificationObserver}
 * 在借車事件發生時呼叫（Observer Pattern）。</p>
 */
@Service
public class NotificationService {

    private final INotificationRepository repo;

    public NotificationService(INotificationRepository repo) {
        this.repo = repo;
    }

    /**
     * 建立一則通知並寫入收件人的收件夾。
     *
     * @param recipientId        收件人使用者 ID
     * @param type               通知類型
     * @param title              標題
     * @param content            內容
     * @param relatedBorrowingId 關聯的借車申請 ID（可為 null）
     * @return 已儲存的通知
     */
    public Notification create(UUID recipientId, String type, String title,
                               String content, UUID relatedBorrowingId) {
        Notification n = new Notification(
                UUID.randomUUID(), recipientId, type, title, content,
                false, relatedBorrowingId, Instant.now());
        return repo.save(n);
    }

    /** 取得指定使用者的收件夾（新到舊）。 */
    public List<Notification> listForUser(UUID userId) {
        return repo.findByRecipient(userId);
    }

    /** 取得指定使用者的未讀通知數量（狀態燈用）。 */
    public long countUnread(UUID userId) {
        return repo.countUnread(userId);
    }

    /**
     * 將指定通知標記為已讀。
     *
     * @throws ResourceNotFoundException 若通知不存在
     * @throws PermissionDeniedException 若通知不屬於該使用者
     */
    public void markAsRead(UUID userId, UUID notificationId) {
        Notification n = repo.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        if (!n.getRecipientId().equals(userId)) {
            throw new PermissionDeniedException("Cannot read another user's notification");
        }
        n.markRead();
        repo.save(n);
    }

    /** 將指定使用者的所有未讀通知標記為已讀。 */
    public void markAllAsRead(UUID userId) {
        repo.findByRecipient(userId).stream()
                .filter(n -> !n.isRead())
                .forEach(n -> {
                    n.markRead();
                    repo.save(n);
                });
    }
}
