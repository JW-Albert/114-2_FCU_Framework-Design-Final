package com.vehicle.management.domain.strategy;

import com.vehicle.management.domain.model.BorrowingRequest;

import java.time.Instant;
import java.util.List;

/**
 * 借車時段衝突檢查策略介面（Strategy Pattern，Ch18）。
 *
 * <p><b>設計動機：</b>衝突判斷的業務規則可能隨需求改變
 * （例如：「嚴格重疊」、「含緩衝時間」、「寬鬆判斷」等），
 * 將演算法封裝為獨立策略類別，可在不修改 Service 層的情況下替換判斷邏輯（OCP）。</p>
 *
 * <p><b>使用方式：</b>{@link com.vehicle.management.service.BorrowingService}
 * 注入此介面的實作，在送出申請時呼叫 {@link #hasConflict} 進行檢查。</p>
 *
 * @see StrictOverlapStrategy
 */
public interface ConflictCheckStrategy {

    /**
     * 判斷指定時段是否與現有借車申請產生衝突。
     *
     * @param existingRequests 同一車輛的現有申請清單
     * @param periodStart      欲借用的開始時間
     * @param periodEnd        欲借用的結束時間
     * @return 若存在任何衝突回傳 {@code true}
     */
    boolean hasConflict(List<BorrowingRequest> existingRequests,
                        Instant periodStart,
                        Instant periodEnd);
}
