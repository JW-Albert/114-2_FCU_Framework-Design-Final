package com.vehicle.management.api.dto;

import java.util.UUID;

public record UserActivityResponse(
        UUID userId,
        String name,
        int requestCount
) {}
