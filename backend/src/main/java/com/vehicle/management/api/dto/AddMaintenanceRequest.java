package com.vehicle.management.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AddMaintenanceRequest(
        @NotNull UUID vehicleId,
        @NotNull LocalDate date,
        @NotNull List<String> items,
        @Positive BigDecimal cost,
        LocalDate nextDueDate,
        Integer nextDueKm
) {}
