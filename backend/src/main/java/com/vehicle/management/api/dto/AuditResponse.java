package com.vehicle.management.api.dto;

import com.vehicle.management.domain.model.AuditLog;

import java.time.Instant;
import java.util.UUID;

public record AuditResponse(
        UUID id,
        String action,
        String detail,
        UUID targetId,
        Instant createdAt
) {
    public static AuditResponse from(AuditLog a) {
        return new AuditResponse(a.getId(), a.getAction(), a.getDetail(), a.getTargetId(), a.getCreatedAt());
    }
}
