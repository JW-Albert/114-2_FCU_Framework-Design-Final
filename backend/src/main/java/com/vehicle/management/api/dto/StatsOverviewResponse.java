package com.vehicle.management.api.dto;

public record StatsOverviewResponse(
        long totalVehicles,
        long availableVehicles,
        long pendingRequests,
        long activeUses,
        long totalUsers
) {}
