package com.vehicle.management.domain.model;

import com.vehicle.management.domain.state.BorrowingState;
import com.vehicle.management.domain.state.PendingState;

import java.time.Instant;
import java.util.UUID;

public class BorrowingRequest {

    private final UUID id;
    private final UUID userId;
    private final UUID vehicleId;
    private final Instant periodStart;
    private final Instant periodEnd;
    private final String purpose;
    private BorrowingState state;
    private String reviewNote;
    private final Instant createdAt;
    private Instant updatedAt;

    public BorrowingRequest(UUID id, UUID userId, UUID vehicleId,
                            Instant periodStart, Instant periodEnd,
                            String purpose, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.vehicleId = vehicleId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.purpose = purpose;
        this.state = new PendingState();
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public void approve(String reviewNote) {
        state.approve(this, reviewNote);
        this.updatedAt = Instant.now();
    }

    public void reject(String reviewNote) {
        state.reject(this, reviewNote);
        this.updatedAt = Instant.now();
    }

    public void startUse() {
        state.startUse(this);
        this.updatedAt = Instant.now();
    }

    public void complete() {
        state.complete(this);
        this.updatedAt = Instant.now();
    }

    public String getStateName() {
        return state.getStateName();
    }

    /** Called only by State pattern implementations to perform a transition. */
    public void transitionState(BorrowingState newState, String note) {
        this.state = newState;
        if (note != null) this.reviewNote = note;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getVehicleId() { return vehicleId; }
    public Instant getPeriodStart() { return periodStart; }
    public Instant getPeriodEnd() { return periodEnd; }
    public String getPurpose() { return purpose; }
    public String getReviewNote() { return reviewNote; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
