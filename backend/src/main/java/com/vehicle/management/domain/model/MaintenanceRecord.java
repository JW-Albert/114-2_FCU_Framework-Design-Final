package com.vehicle.management.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * 車輛保養紀錄領域物件（Domain Entity）。
 *
 * <p>封裝「下次保養是否到期」的判斷邏輯（{@link #isDue}），
 * 符合單一職責原則（SRP）—— 業務規則由領域物件自行持有，
 * 而非散落在 Service 或 Controller 層。</p>
 *
 * <p>提醒到期判斷同時支援<b>日期</b>與<b>里程</b>兩種條件，任一達標即提醒。</p>
 */
public class MaintenanceRecord {

    private final UUID id;
    private final UUID vehicleId;
    /** 本次保養執行日期。 */
    private final LocalDate date;
    /** 本次保養項目清單（如「換機油」、「換剎車皮」）。 */
    private final List<String> items;
    /** 本次保養費用。 */
    private final BigDecimal cost;
    /** 下次建議保養日期（可為 {@code null} 表示不設定）。 */
    private final LocalDate nextDueDate;
    /** 下次建議保養里程（可為 {@code null} 表示不設定）。 */
    private final Integer nextDueKm;
    private final Instant createdAt;

    /**
     * 建構保養紀錄物件。
     *
     * @param id          紀錄唯一識別碼
     * @param vehicleId   所屬車輛 ID
     * @param date        本次保養日期
     * @param items       保養項目清單（不可變副本）
     * @param cost        保養費用
     * @param nextDueDate 下次保養建議日期（可為 {@code null}）
     * @param nextDueKm   下次保養建議里程（可為 {@code null}）
     * @param createdAt   紀錄建立時間
     */
    public MaintenanceRecord(UUID id, UUID vehicleId, LocalDate date,
                             List<String> items, BigDecimal cost,
                             LocalDate nextDueDate, Integer nextDueKm,
                             Instant createdAt) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.date = date;
        this.items = List.copyOf(items);  // 防禦性複製，確保不可變
        this.cost = cost;
        this.nextDueDate = nextDueDate;
        this.nextDueKm = nextDueKm;
        this.createdAt = createdAt;
    }

    /**
     * 判斷此保養紀錄的下次保養是否已到期。
     *
     * <p>判斷邏輯（OR 條件）：
     * <ul>
     *   <li>設定了 {@code nextDueDate} 且 {@code currentDate} 已達或超過該日期；或</li>
     *   <li>設定了 {@code nextDueKm} 且 {@code currentKm} 已達或超過該里程。</li>
     * </ul>
     * 未設定的條件（{@code null}）不計入判斷。</p>
     *
     * @param currentDate 查詢當下的日期
     * @param currentKm   車輛目前的里程數
     * @return 若到期回傳 {@code true}
     */
    public boolean isDue(LocalDate currentDate, int currentKm) {
        boolean dateDue = nextDueDate != null && !currentDate.isBefore(nextDueDate);
        boolean kmDue   = nextDueKm   != null && currentKm >= nextDueKm;
        return dateDue || kmDue;
    }

    /** @return 紀錄唯一識別碼 */
    public UUID getId() { return id; }

    /** @return 所屬車輛 ID */
    public UUID getVehicleId() { return vehicleId; }

    /** @return 本次保養日期 */
    public LocalDate getDate() { return date; }

    /** @return 保養項目清單（不可變） */
    public List<String> getItems() { return items; }

    /** @return 保養費用 */
    public BigDecimal getCost() { return cost; }

    /** @return 下次建議保養日期（可為 {@code null}） */
    public LocalDate getNextDueDate() { return nextDueDate; }

    /** @return 下次建議保養里程（可為 {@code null}） */
    public Integer getNextDueKm() { return nextDueKm; }

    /** @return 紀錄建立時間 */
    public Instant getCreatedAt() { return createdAt; }
}
