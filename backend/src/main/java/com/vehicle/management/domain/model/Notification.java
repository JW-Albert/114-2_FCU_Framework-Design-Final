package com.vehicle.management.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * 站內通知（收件夾訊息）領域物件。
 *
 * <p>由 {@link com.vehicle.management.domain.observer.InboxNotificationObserver}
 * 在借車事件發生時建立，寫入收件人的收件夾。除 {@code read}（已讀狀態）外皆為不可變欄位。</p>
 */
public class Notification {

    private final UUID id;
    /** 收件人使用者 ID。 */
    private final UUID recipientId;
    /** 通知類型，例如 BORROWING_SUBMITTED、BORROWING_APPROVED。 */
    private final String type;
    private final String title;
    private final String content;
    /** 已讀狀態，唯一可變欄位。 */
    private boolean read;
    /** 關聯的借車申請 ID（可為 null）。 */
    private final UUID relatedBorrowingId;
    private final Instant createdAt;

    public Notification(UUID id, UUID recipientId, String type, String title,
                        String content, boolean read, UUID relatedBorrowingId, Instant createdAt) {
        this.id = id;
        this.recipientId = recipientId;
        this.type = type;
        this.title = title;
        this.content = content;
        this.read = read;
        this.relatedBorrowingId = relatedBorrowingId;
        this.createdAt = createdAt;
    }

    /** 標記為已讀。 */
    public void markRead() {
        this.read = true;
    }

    public UUID getId() { return id; }
    public UUID getRecipientId() { return recipientId; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public boolean isRead() { return read; }
    public UUID getRelatedBorrowingId() { return relatedBorrowingId; }
    public Instant getCreatedAt() { return createdAt; }
}
