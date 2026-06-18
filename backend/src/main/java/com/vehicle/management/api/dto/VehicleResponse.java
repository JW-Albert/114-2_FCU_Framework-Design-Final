package com.vehicle.management.api.dto;

import com.vehicle.management.domain.model.Vehicle;

import java.time.Instant;
import java.util.UUID;

public record VehicleResponse(
        UUID id,
        String plate,
        String model,
        int year,
        String status,
        Instant createdAt,
        int currentMileage
) {
    public static VehicleResponse from(Vehicle v) {
        return new VehicleResponse(v.getId(), v.getPlate(), v.getModel(),
                v.getYear(), v.getStatus().name(), v.getCreatedAt(), v.getCurrentMileage());
    }
}
