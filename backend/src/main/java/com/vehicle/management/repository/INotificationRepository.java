package com.vehicle.management.repository;

import com.vehicle.management.domain.model.Notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 站內通知儲存庫介面（DIP）。
 *
 * <p>業務層依賴此介面，不感知具體 JPA 實作。</p>
 */
public interface INotificationRepository {

    /** 儲存（新增或更新）通知。 */
    Notification save(Notification notification);

    /** 依 ID 查詢通知。 */
    Optional<Notification> findById(UUID id);

    /** 取得收件人的通知，依建立時間由新到舊排序。 */
    List<Notification> findByRecipient(UUID recipientId);

    /** 計算收件人的未讀通知數量。 */
    long countUnread(UUID recipientId);
}
