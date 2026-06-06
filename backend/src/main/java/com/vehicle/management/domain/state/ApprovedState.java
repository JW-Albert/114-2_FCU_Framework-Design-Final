package com.vehicle.management.domain.state;

import com.vehicle.management.domain.model.BorrowingRequest;

public class ApprovedState implements BorrowingState {

    @Override
    public void approve(BorrowingRequest request, String reviewNote) {
        throw new InvalidStateTransitionException(getStateName(), "approve");
    }

    @Override
    public void reject(BorrowingRequest request, String reviewNote) {
        throw new InvalidStateTransitionException(getStateName(), "reject");
    }

    @Override
    public void startUse(BorrowingRequest request) {
        request.setState(new InUseState());
    }

    @Override
    public void complete(BorrowingRequest request) {
        throw new InvalidStateTransitionException(getStateName(), "complete");
    }

    @Override
    public String getStateName() { return "APPROVED"; }
}
