package com.vehicle.management.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * 違規記錄領域物件（Domain Entity）。
 *
 * <p>封裝違規資訊，目前支援「超時還車（OVERDUE）」類型。
 * 由 {@link com.vehicle.management.service.ViolationService} 在還車時自動建立。</p>
 */
public class ViolationRecord {

    private final UUID id;
    private final UUID userId;
    private final UUID vehicleId;
    private final UUID borrowingId;
    /** 違規類型，例如 "OVERDUE"（超時還車）。 */
    private final String type;
    /** 違規描述，包含超時分鐘數與時間資訊。 */
    private final String description;
    private final Instant createdAt;

    /**
     * 建構違規記錄物件。
     *
     * @param id          記錄唯一識別碼
     * @param userId      違規使用者 ID
     * @param vehicleId   違規車輛 ID
     * @param borrowingId 對應的借車申請 ID
     * @param type        違規類型（如 "OVERDUE"）
     * @param description 違規描述
     * @param createdAt   記錄建立時間
     */
    public ViolationRecord(UUID id, UUID userId, UUID vehicleId, UUID borrowingId,
                           String type, String description, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.vehicleId = vehicleId;
        this.borrowingId = borrowingId;
        this.type = type;
        this.description = description;
        this.createdAt = createdAt;
    }

    /** @return 記錄唯一識別碼 */
    public UUID getId() { return id; }

    /** @return 違規使用者 ID */
    public UUID getUserId() { return userId; }

    /** @return 違規車輛 ID */
    public UUID getVehicleId() { return vehicleId; }

    /** @return 對應的借車申請 ID */
    public UUID getBorrowingId() { return borrowingId; }

    /** @return 違規類型 */
    public String getType() { return type; }

    /** @return 違規描述 */
    public String getDescription() { return description; }

    /** @return 記錄建立時間 */
    public Instant getCreatedAt() { return createdAt; }
}
