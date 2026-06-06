package com.vehicle.management.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record SubmitBorrowingRequest(
        @NotNull UUID vehicleId,
        @NotNull Instant periodStart,
        @NotNull Instant periodEnd,
        @NotBlank String purpose
) {}
