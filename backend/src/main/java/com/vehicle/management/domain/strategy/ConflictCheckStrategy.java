package com.vehicle.management.domain.strategy;

import com.vehicle.management.domain.model.BorrowingRequest;

import java.time.Instant;
import java.util.List;

/**
 * Strategy (Ch18): 借車時段衝突檢查策略介面。
 * 封裝各種衝突判斷演算法，使其可在執行期互換（OCP）。
 */
public interface ConflictCheckStrategy {
    boolean hasConflict(List<BorrowingRequest> existingRequests,
                        Instant periodStart,
                        Instant periodEnd);
}
