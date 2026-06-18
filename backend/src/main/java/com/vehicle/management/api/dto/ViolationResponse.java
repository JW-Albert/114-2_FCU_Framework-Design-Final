package com.vehicle.management.api.dto;

import com.vehicle.management.domain.model.ViolationRecord;

import java.time.Instant;
import java.util.UUID;

public record ViolationResponse(
        UUID id,
        UUID userId,
        UUID vehicleId,
        UUID borrowingId,
        String type,
        String description,
        Instant createdAt
) {
    public static ViolationResponse from(ViolationRecord v) {
        return new ViolationResponse(
                v.getId(), v.getUserId(), v.getVehicleId(), v.getBorrowingId(),
                v.getType(), v.getDescription(), v.getCreatedAt());
    }
}
