package com.vehicle.management.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * 稽核日誌領域物件（不可變）。
 *
 * <p>記錄一次經由 {@link com.vehicle.management.service.BorrowingCommandBus} 派送的命令，
 * 提供借車生命週期操作的可追溯紀錄。</p>
 */
public class AuditLog {

    private final UUID id;
    /** 操作類型，例如 ApproveCommand、StartUseCommand。 */
    private final String action;
    /** 操作明細（命令的 describe()，含執行者與目標等資訊）。 */
    private final String detail;
    /** 受影響的目標 ID（借車申請 ID）。 */
    private final UUID targetId;
    private final Instant createdAt;

    public AuditLog(UUID id, String action, String detail, UUID targetId, Instant createdAt) {
        this.id = id;
        this.action = action;
        this.detail = detail;
        this.targetId = targetId;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getAction() { return action; }
    public String getDetail() { return detail; }
    public UUID getTargetId() { return targetId; }
    public Instant getCreatedAt() { return createdAt; }
}
