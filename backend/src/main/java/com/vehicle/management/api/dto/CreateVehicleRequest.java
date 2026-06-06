package com.vehicle.management.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateVehicleRequest(
        @NotBlank String plate,
        @NotBlank String model,
        @Positive int year
) {}
