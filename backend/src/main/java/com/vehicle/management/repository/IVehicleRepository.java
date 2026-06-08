package com.vehicle.management.repository;

import com.vehicle.management.domain.model.Vehicle;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 車輛儲存庫介面（Repository Interface）。
 *
 * <p><b>依賴反轉原則（DIP）：</b>Service 層依賴此介面，不依賴具體的 JPA 或 InMemory 實作，
 * 使業務邏輯可在不同儲存機制之間自由切換。</p>
 */
public interface IVehicleRepository {

    /**
     * 依 ID 查詢車輛。
     *
     * @param id 車輛唯一識別碼
     * @return 包含車輛的 {@link Optional}，若不存在則為空
     */
    Optional<Vehicle> findById(UUID id);

    /**
     * 查詢所有車輛。
     *
     * @return 系統中所有車輛清單
     */
    List<Vehicle> findAll();

    /**
     * 查詢在指定時段內可借用的車輛。
     *
     * @param periodStart 借用開始時間
     * @param periodEnd   借用結束時間
     * @return 在該時段無衝突借車申請的可用車輛清單
     */
    List<Vehicle> findAvailable(Instant periodStart, Instant periodEnd);

    /**
     * 儲存車輛（新增或更新）。
     *
     * @param vehicle 欲儲存的車輛物件
     * @return 儲存後的車輛物件
     */
    Vehicle save(Vehicle vehicle);

    /**
     * 依 ID 刪除車輛。
     *
     * @param id 欲刪除的車輛唯一識別碼
     */
    void delete(UUID id);
}
