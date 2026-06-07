package com.vehicle.management.domain.strategy;

import com.vehicle.management.domain.model.BorrowingRequest;

import java.time.Instant;
import java.util.List;

/**
 * ConcreteStrategy (Ch18): 嚴格時段重疊檢查。
 * 只要時段有任何重疊即視為衝突。
 */
public class StrictOverlapStrategy implements ConflictCheckStrategy {

    @Override
    public boolean hasConflict(List<BorrowingRequest> existingRequests,
                               Instant periodStart,
                               Instant periodEnd) {
        return existingRequests.stream()
                .filter(r -> isActiveState(r.getStateName()))
                .anyMatch(r -> periodStart.isBefore(r.getPeriodEnd())
                            && periodEnd.isAfter(r.getPeriodStart()));
    }

    private boolean isActiveState(String state) {
        return "PENDING".equals(state) || "APPROVED".equals(state) || "IN_USE".equals(state);
    }
}
