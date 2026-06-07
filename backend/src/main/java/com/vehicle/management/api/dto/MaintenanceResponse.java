package com.vehicle.management.api.dto;

import com.vehicle.management.domain.model.MaintenanceRecord;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record MaintenanceResponse(
        UUID id,
        UUID vehicleId,
        LocalDate date,
        List<String> items,
        BigDecimal cost,
        LocalDate nextDueDate,
        Integer nextDueKm,
        Instant createdAt
) {
    public static MaintenanceResponse from(MaintenanceRecord r) {
        return new MaintenanceResponse(
                r.getId(), r.getVehicleId(), r.getDate(), r.getItems(),
                r.getCost(), r.getNextDueDate(), r.getNextDueKm(), r.getCreatedAt());
    }
}
