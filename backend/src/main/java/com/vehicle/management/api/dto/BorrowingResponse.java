package com.vehicle.management.api.dto;

import com.vehicle.management.domain.model.BorrowingRequest;

import java.time.Instant;
import java.util.UUID;

public record BorrowingResponse(
        UUID id,
        UUID userId,
        UUID vehicleId,
        Instant periodStart,
        Instant periodEnd,
        String purpose,
        String state,
        String reviewNote,
        Instant createdAt,
        Instant updatedAt
) {
    public static BorrowingResponse from(BorrowingRequest r) {
        return new BorrowingResponse(
                r.getId(), r.getUserId(), r.getVehicleId(),
                r.getPeriodStart(), r.getPeriodEnd(), r.getPurpose(),
                r.getStateName(), r.getReviewNote(),
                r.getCreatedAt(), r.getUpdatedAt());
    }
}
