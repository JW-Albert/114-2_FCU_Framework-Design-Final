package com.vehicle.management.domain.state;

import com.vehicle.management.domain.model.BorrowingRequest;

public class PendingState implements BorrowingState {

    @Override
    public void approve(BorrowingRequest request, String reviewNote) {
        request.transitionState(new ApprovedState(), reviewNote);
    }

    @Override
    public void reject(BorrowingRequest request, String reviewNote) {
        request.transitionState(new RejectedState(), reviewNote);
    }

    @Override
    public void startUse(BorrowingRequest request) {
        throw new InvalidStateTransitionException(getStateName(), "startUse");
    }

    @Override
    public void complete(BorrowingRequest request) {
        throw new InvalidStateTransitionException(getStateName(), "complete");
    }

    @Override
    public String getStateName() { return "PENDING"; }
}
