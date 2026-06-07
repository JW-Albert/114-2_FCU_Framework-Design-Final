package com.vehicle.management.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class MaintenanceRecord {

    private final UUID id;
    private final UUID vehicleId;
    private final LocalDate date;
    private final List<String> items;
    private final BigDecimal cost;
    private final LocalDate nextDueDate;
    private final Integer nextDueKm;
    private final Instant createdAt;

    public MaintenanceRecord(UUID id, UUID vehicleId, LocalDate date,
                             List<String> items, BigDecimal cost,
                             LocalDate nextDueDate, Integer nextDueKm,
                             Instant createdAt) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.date = date;
        this.items = List.copyOf(items);
        this.cost = cost;
        this.nextDueDate = nextDueDate;
        this.nextDueKm = nextDueKm;
        this.createdAt = createdAt;
    }

    public boolean isDue(LocalDate currentDate, int currentKm) {
        boolean dateDue = nextDueDate != null && !currentDate.isBefore(nextDueDate);
        boolean kmDue = nextDueKm != null && currentKm >= nextDueKm;
        return dateDue || kmDue;
    }

    public UUID getId() { return id; }
    public UUID getVehicleId() { return vehicleId; }
    public LocalDate getDate() { return date; }
    public List<String> getItems() { return items; }
    public BigDecimal getCost() { return cost; }
    public LocalDate getNextDueDate() { return nextDueDate; }
    public Integer getNextDueKm() { return nextDueKm; }
    public Instant getCreatedAt() { return createdAt; }
}
