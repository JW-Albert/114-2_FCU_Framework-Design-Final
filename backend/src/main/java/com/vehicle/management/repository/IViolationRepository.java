package com.vehicle.management.repository;

import com.vehicle.management.domain.model.ViolationRecord;

import java.util.List;
import java.util.UUID;

/**
 * 違規記錄儲存庫介面（Repository Interface）。
 *
 * <p>遵循 DIP（依賴反轉原則）——業務層依賴此介面，
 * 而非依賴具體的 JPA 實作。</p>
 */
public interface IViolationRepository {

    /**
     * 儲存違規記錄。
     *
     * @param v 違規記錄領域物件
     * @return 已儲存的違規記錄（含資料庫產生的 ID 與時間戳）
     */
    ViolationRecord save(ViolationRecord v);

    /**
     * 取得所有違規記錄。
     *
     * @return 全部違規記錄清單
     */
    List<ViolationRecord> findAll();

    /**
     * 依使用者 ID 查詢違規記錄。
     *
     * @param userId 使用者唯一識別碼
     * @return 該使用者的違規記錄清單
     */
    List<ViolationRecord> findByUserId(UUID userId);
}
