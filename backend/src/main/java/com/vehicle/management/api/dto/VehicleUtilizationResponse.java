package com.vehicle.management.api.dto;

import java.util.UUID;

public record VehicleUtilizationResponse(
        UUID vehicleId,
        String plate,
        String model,
        int usageCount,
        long totalDays
) {}
