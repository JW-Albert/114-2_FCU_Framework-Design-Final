package com.vehicle.management.repository;

import com.vehicle.management.domain.model.MaintenanceRecord;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 保養紀錄儲存庫介面（Repository Interface）。
 *
 * <p><b>依賴反轉原則（DIP）：</b>{@link com.vehicle.management.service.MaintenanceService}
 * 依賴此介面，不感知底層儲存實作，
 * 使保養到期提醒等業務邏輯可獨立於資料庫進行單元測試。</p>
 */
public interface IMaintenanceRepository {

    /**
     * 依 ID 查詢保養紀錄。
     *
     * @param id 紀錄唯一識別碼
     * @return 包含紀錄的 {@link Optional}，若不存在則為空
     */
    Optional<MaintenanceRecord> findById(UUID id);

    /**
     * 查詢指定車輛的所有保養紀錄。
     *
     * @param vehicleId 車輛唯一識別碼
     * @return 該車輛的保養紀錄清單（依日期排序建議由實作類別處理）
     */
    List<MaintenanceRecord> findByVehicleId(UUID vehicleId);

    /**
     * 查詢所有保養紀錄（供提醒掃描使用）。
     *
     * @return 系統中所有保養紀錄
     */
    List<MaintenanceRecord> findAll();

    /**
     * 儲存保養紀錄（新增或更新）。
     *
     * @param record 欲儲存的保養紀錄
     * @return 儲存後的保養紀錄物件
     */
    MaintenanceRecord save(MaintenanceRecord record);

    /**
     * 依 ID 刪除保養紀錄。
     *
     * @param id 欲刪除的紀錄唯一識別碼
     */
    void delete(UUID id);
}
