package com.vehicle.management.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * 車輛領域物件（Domain Entity）。
 *
 * <p><b>OOP 設計重點：</b>
 * <ul>
 *   <li><b>狀態守衛（State Guard）</b>：{@link #markInUse()}、{@link #markAvailable()}、
 *       {@link #markMaintenance()} 各自在方法內部驗證合法轉換，
 *       違法操作立即拋出 {@link IllegalStateException}，保護物件不進入無效狀態。</li>
 *   <li><b>與 VehicleStatus 分離</b>：狀態語意定義於 {@link VehicleStatus} 列舉，
 *       Vehicle 只負責守衛轉換規則，符合單一職責原則（SRP）。</li>
 * </ul>
 *
 * <p><b>合法狀態轉換：</b>
 * <pre>
 *   AVAILABLE  ──markInUse()──▶  IN_USE
 *   IN_USE     ──markAvailable()▶ AVAILABLE
 *   AVAILABLE  ──markMaintenance()▶ MAINTENANCE
 *   MAINTENANCE──markAvailable()▶ AVAILABLE
 * </pre>
 */
public class Vehicle {

    private final UUID id;
    private final String plate;
    private final String model;
    private final int year;
    /** 可變狀態欄位，僅透過狀態守衛方法修改。 */
    private VehicleStatus status;
    private final Instant createdAt;
    /** 車輛目前累積里程（公里），預設為 0。 */
    private int currentMileage;

    /**
     * 建構車輛物件。
     *
     * @param id        車輛唯一識別碼
     * @param plate     車牌號碼（應全域唯一）
     * @param model     車款名稱（如「Toyota Camry」）
     * @param year      出廠年份
     * @param status    初始狀態，通常為 {@link VehicleStatus#AVAILABLE}
     * @param createdAt 建立時間
     */
    public Vehicle(UUID id, String plate, String model, int year,
                   VehicleStatus status, Instant createdAt) {
        this(id, plate, model, year, status, createdAt, 0);
    }

    /**
     * 建構車輛物件（含里程）。
     *
     * @param id             車輛唯一識別碼
     * @param plate          車牌號碼
     * @param model          車款名稱
     * @param year           出廠年份
     * @param status         初始狀態
     * @param createdAt      建立時間
     * @param currentMileage 目前累積里程
     */
    public Vehicle(UUID id, String plate, String model, int year,
                   VehicleStatus status, Instant createdAt, int currentMileage) {
        this.id = id;
        this.plate = plate;
        this.model = model;
        this.year = year;
        this.status = status;
        this.createdAt = createdAt;
        this.currentMileage = currentMileage;
    }

    /**
     * 將車輛標記為使用中（出車）。
     *
     * @throws IllegalStateException 若車輛目前狀態不是 {@link VehicleStatus#AVAILABLE}
     */
    public void markInUse() {
        if (status != VehicleStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Vehicle " + plate + " is not AVAILABLE (current: " + status + ")");
        }
        status = VehicleStatus.IN_USE;
    }

    /**
     * 將車輛標記為可用（還車或保養結束）。
     *
     * @throws IllegalStateException 若車輛目前狀態已是 {@link VehicleStatus#AVAILABLE}
     */
    public void markAvailable() {
        if (status == VehicleStatus.AVAILABLE) {
            throw new IllegalStateException("Vehicle " + plate + " is already AVAILABLE");
        }
        status = VehicleStatus.AVAILABLE;
    }

    /**
     * 將車輛標記為維修保養中。
     *
     * @throws IllegalStateException 若車輛目前正在使用中（{@link VehicleStatus#IN_USE}），
     *                               不允許直接進入保養狀態
     */
    public void markMaintenance() {
        if (status == VehicleStatus.IN_USE) {
            throw new IllegalStateException(
                    "Cannot set MAINTENANCE while vehicle is IN_USE");
        }
        status = VehicleStatus.MAINTENANCE;
    }

    /** @return 車輛唯一識別碼 */
    public UUID getId() { return id; }

    /** @return 車牌號碼 */
    public String getPlate() { return plate; }

    /** @return 車款名稱 */
    public String getModel() { return model; }

    /** @return 出廠年份 */
    public int getYear() { return year; }

    /** @return 車輛目前狀態 */
    public VehicleStatus getStatus() { return status; }

    /** @return 車輛資料建立時間 */
    public Instant getCreatedAt() { return createdAt; }

    /** @return 車輛目前累積里程（公里） */
    public int getCurrentMileage() { return currentMileage; }

    /**
     * 更新車輛累積里程。
     *
     * @param km 新的里程數（必須 &gt;= 目前里程）
     * @throws IllegalArgumentException 若 km 小於目前里程（里程不可倒退）
     */
    public void updateMileage(int km) {
        if (km < currentMileage) {
            throw new IllegalArgumentException(
                    "New mileage " + km + " is less than current mileage " + currentMileage);
        }
        this.currentMileage = km;
    }
}
