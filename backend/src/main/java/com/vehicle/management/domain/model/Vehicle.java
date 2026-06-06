package com.vehicle.management.domain.model;

import java.time.Instant;
import java.util.UUID;

public class Vehicle {

    private final UUID id;
    private final String plate;
    private final String model;
    private final int year;
    private VehicleStatus status;
    private final Instant createdAt;

    public Vehicle(UUID id, String plate, String model, int year,
                   VehicleStatus status, Instant createdAt) {
        this.id = id;
        this.plate = plate;
        this.model = model;
        this.year = year;
        this.status = status;
        this.createdAt = createdAt;
    }

    public void markInUse() {
        if (status != VehicleStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Vehicle " + plate + " is not AVAILABLE (current: " + status + ")");
        }
        status = VehicleStatus.IN_USE;
    }

    public void markAvailable() {
        if (status == VehicleStatus.AVAILABLE) {
            throw new IllegalStateException("Vehicle " + plate + " is already AVAILABLE");
        }
        status = VehicleStatus.AVAILABLE;
    }

    public void markMaintenance() {
        if (status == VehicleStatus.IN_USE) {
            throw new IllegalStateException(
                    "Cannot set MAINTENANCE while vehicle is IN_USE");
        }
        status = VehicleStatus.MAINTENANCE;
    }

    public UUID getId() { return id; }
    public String getPlate() { return plate; }
    public String getModel() { return model; }
    public int getYear() { return year; }
    public VehicleStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
