package com.vehicle.management.api.dto;

public record MonthlyStatsResponse(
        String month,
        int count
) {}
