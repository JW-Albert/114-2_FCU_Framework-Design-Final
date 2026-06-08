package com.vehicle.management.repository;

import com.vehicle.management.domain.model.BorrowingRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 借車申請儲存庫介面（Repository Interface）。
 *
 * <p><b>依賴反轉原則（DIP）：</b>{@link com.vehicle.management.service.BorrowingService}
 * 依賴此介面，不感知底層儲存實作（JPA 或 InMemory），
 * 使衝突查詢、分頁等邏輯可在不同儲存機制下一致運作。</p>
 */
public interface IBorrowingRepository {

    /**
     * 依 ID 查詢借車申請。
     *
     * @param id 申請單唯一識別碼
     * @return 包含申請的 {@link Optional}，若不存在則為空
     */
    Optional<BorrowingRequest> findById(UUID id);

    /**
     * 查詢所有借車申請。
     *
     * @return 系統中所有借車申請清單
     */
    List<BorrowingRequest> findAll();

    /**
     * 查詢所有待審核（PENDING）的借車申請。
     *
     * @return 狀態為 PENDING 的申請清單
     */
    List<BorrowingRequest> findPending();

    /**
     * 查詢指定使用者的所有借車申請。
     *
     * @param userId 使用者唯一識別碼
     * @return 該使用者的申請清單
     */
    List<BorrowingRequest> findByUserId(UUID userId);

    /**
     * 查詢指定車輛在給定時段內的衝突申請。
     * 衝突判斷由 {@link com.vehicle.management.domain.strategy.ConflictCheckStrategy} 執行。
     *
     * @param vehicleId 車輛唯一識別碼
     * @param start     時段開始時間
     * @param end       時段結束時間
     * @return 時段內的申請清單（含各種狀態，由策略類別過濾）
     */
    List<BorrowingRequest> findConflicting(UUID vehicleId, Instant start, Instant end);

    /**
     * 儲存借車申請（新增或更新）。
     *
     * @param request 欲儲存的借車申請
     * @return 儲存後的借車申請物件
     */
    BorrowingRequest save(BorrowingRequest request);
}
