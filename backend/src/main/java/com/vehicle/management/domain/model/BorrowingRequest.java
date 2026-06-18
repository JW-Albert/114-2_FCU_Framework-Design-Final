package com.vehicle.management.domain.model;

import com.vehicle.management.domain.state.BorrowingState;
import com.vehicle.management.domain.state.PendingState;

import java.time.Instant;
import java.util.UUID;

/**
 * 借車申請領域物件（Domain Entity）。
 *
 * <p><b>State Pattern（Ch13）應用：</b><br>
 * 每個生命週期操作（{@link #approve}、{@link #reject}、{@link #startUse}、{@link #complete}）
 * 都委派給目前的 {@link BorrowingState} 實作，由狀態物件決定是否允許轉換。
 * 這使得 {@code BorrowingRequest} 本身無需 {@code if/switch} 判斷目前狀態，
 * 符合開放封閉原則（OCP）—— 新增狀態只需新增一個 {@code BorrowingState} 實作。</p>
 *
 * <p><b>狀態轉換圖：</b>
 * <pre>
 *   [建立] ──▶ PENDING ──approve()──▶ APPROVED ──startUse()──▶ IN_USE ──complete()──▶ RETURNED
 *                     └──reject()──▶ REJECTED
 * </pre>
 * 非法轉換（例如對已還車的申請再次核准）將拋出
 * {@link com.vehicle.management.domain.state.InvalidStateTransitionException}。</p>
 */
public class BorrowingRequest {

    private final UUID id;
    private final UUID userId;
    private final UUID vehicleId;
    private final Instant periodStart;
    private final Instant periodEnd;
    private final String purpose;
    /** 目前狀態，由 State Pattern 物件持有並負責轉換。 */
    private BorrowingState state;
    /** 管理員填寫的審核備註（核准或拒絕原因）。 */
    private String reviewNote;
    private final Instant createdAt;
    private Instant updatedAt;
    /** 出車時記錄的起始里程（可為 null）。 */
    private Integer startMileage;
    /** 還車時記錄的結束里程（可為 null）。 */
    private Integer endMileage;

    /**
     * 建構新的借車申請，初始狀態為 {@link PendingState}（待審核）。
     *
     * @param id          申請單唯一識別碼
     * @param userId      申請人的使用者 ID
     * @param vehicleId   欲借用的車輛 ID
     * @param periodStart 借車開始時間
     * @param periodEnd   借車結束時間
     * @param purpose     借車事由說明
     * @param createdAt   申請建立時間
     */
    public BorrowingRequest(UUID id, UUID userId, UUID vehicleId,
                            Instant periodStart, Instant periodEnd,
                            String purpose, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.vehicleId = vehicleId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.purpose = purpose;
        this.state = new PendingState();  // 初始狀態：待審核
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    /**
     * 核准此申請。
     * 委派給目前的 {@link BorrowingState#approve} 執行狀態轉換。
     *
     * @param reviewNote 管理員核准備註
     * @throws com.vehicle.management.domain.state.InvalidStateTransitionException
     *         若目前狀態不允許核准操作（例如已是 APPROVED 或 RETURNED）
     */
    public void approve(String reviewNote) {
        state.approve(this, reviewNote);
        this.updatedAt = Instant.now();
    }

    /**
     * 拒絕此申請。
     * 委派給目前的 {@link BorrowingState#reject} 執行狀態轉換。
     *
     * @param reviewNote 管理員拒絕原因
     * @throws com.vehicle.management.domain.state.InvalidStateTransitionException
     *         若目前狀態不允許拒絕操作
     */
    public void reject(String reviewNote) {
        state.reject(this, reviewNote);
        this.updatedAt = Instant.now();
    }

    /**
     * 執行出車（標記為使用中）。
     * 委派給目前的 {@link BorrowingState#startUse} 執行狀態轉換。
     *
     * @throws com.vehicle.management.domain.state.InvalidStateTransitionException
     *         若目前狀態不是 APPROVED
     */
    public void startUse() {
        state.startUse(this);
        this.updatedAt = Instant.now();
    }

    /**
     * 完成用車並還車，記錄結束里程。
     * 委派給目前的 {@link BorrowingState#complete} 執行狀態轉換。
     *
     * @param endMileage 還車時的里程數
     * @throws com.vehicle.management.domain.state.InvalidStateTransitionException
     *         若目前狀態不是 IN_USE
     */
    public void complete(int endMileage) {
        state.complete(this);
        this.endMileage = endMileage;
        this.updatedAt = Instant.now();
    }

    /**
     * 取得目前狀態名稱字串（例如 "PENDING"、"APPROVED"）。
     *
     * @return 狀態名稱
     */
    public String getStateName() {
        return state.getStateName();
    }

    /**
     * 由 State Pattern 實作類別呼叫，執行實際的狀態物件替換與備註寫入。
     * <b>外部程式碼不應直接呼叫此方法</b>，應使用 {@link #approve}、{@link #reject} 等語意方法。
     *
     * @param newState 新的狀態物件
     * @param note     審核備註（可為 {@code null}）
     */
    public void transitionState(BorrowingState newState, String note) {
        this.state = newState;
        if (note != null) this.reviewNote = note;
    }

    /** @return 申請單唯一識別碼 */
    public UUID getId() { return id; }

    /** @return 申請人使用者 ID */
    public UUID getUserId() { return userId; }

    /** @return 欲借用的車輛 ID */
    public UUID getVehicleId() { return vehicleId; }

    /** @return 借車開始時間 */
    public Instant getPeriodStart() { return periodStart; }

    /** @return 借車結束時間 */
    public Instant getPeriodEnd() { return periodEnd; }

    /** @return 借車事由 */
    public String getPurpose() { return purpose; }

    /** @return 管理員填寫的審核備註（可能為 {@code null}） */
    public String getReviewNote() { return reviewNote; }

    /** @return 申請建立時間 */
    public Instant getCreatedAt() { return createdAt; }

    /** @return 最後更新時間 */
    public Instant getUpdatedAt() { return updatedAt; }

    /** @return 出車時的起始里程（可能為 {@code null}） */
    public Integer getStartMileage() { return startMileage; }

    /** @return 還車時的結束里程（可能為 {@code null}） */
    public Integer getEndMileage() { return endMileage; }

    /**
     * 由 Adapter 呼叫，從資料庫還原里程資料。
     *
     * @param startMileage 起始里程
     * @param endMileage   結束里程
     */
    public void restoreMileage(Integer startMileage, Integer endMileage) {
        this.startMileage = startMileage;
        this.endMileage = endMileage;
    }
}
