package com.vehicle.management.domain.strategy;

import com.vehicle.management.domain.model.BorrowingRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * 嚴格時段重疊策略（ConcreteStrategy，Strategy Pattern Ch18）。
 *
 * <p>檢查新申請的時段是否與任何狀態為 PENDING、APPROVED 或 IN_USE 的現有申請重疊。
 * 「重疊」的定義為：兩個時段在時間軸上有任何交集（半開區間計算）。</p>
 *
 * <p><b>判斷公式：</b>{@code newStart < existingEnd && newEnd > existingStart}</p>
 */
@Component
public class StrictOverlapStrategy implements ConflictCheckStrategy {

    /**
     * 判斷指定時段是否與有效申請（PENDING/APPROVED/IN_USE）重疊。
     *
     * @param existingRequests 同一車輛的現有申請清單
     * @param periodStart      欲借用的開始時間
     * @param periodEnd        欲借用的結束時間
     * @return 若存在時段重疊的有效申請回傳 {@code true}
     */
    @Override
    public boolean hasConflict(List<BorrowingRequest> existingRequests,
                               Instant periodStart,
                               Instant periodEnd) {
        return existingRequests.stream()
                .filter(r -> isActiveState(r.getStateName()))  // 只考慮進行中的申請
                .anyMatch(r -> periodStart.isBefore(r.getPeriodEnd())
                            && periodEnd.isAfter(r.getPeriodStart()));
    }

    /**
     * 判斷申請狀態是否為「進行中」（會佔用車輛的狀態）。
     *
     * @param state 狀態名稱字串
     * @return 若為 PENDING、APPROVED 或 IN_USE 回傳 {@code true}
     */
    private boolean isActiveState(String state) {
        return "PENDING".equals(state) || "APPROVED".equals(state) || "IN_USE".equals(state);
    }
}
