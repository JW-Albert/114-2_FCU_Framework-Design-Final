package com.vehicle.management.domain.state;

import com.vehicle.management.domain.model.BorrowingRequest;

public class RejectedState implements BorrowingState {

    @Override
    public void approve(BorrowingRequest r, String n) {
        throw new InvalidStateTransitionException(getStateName(), "approve");
    }

    @Override
    public void reject(BorrowingRequest r, String n) {
        throw new InvalidStateTransitionException(getStateName(), "reject");
    }

    @Override
    public void startUse(BorrowingRequest r) {
        throw new InvalidStateTransitionException(getStateName(), "startUse");
    }

    @Override
    public void complete(BorrowingRequest r) {
        throw new InvalidStateTransitionException(getStateName(), "complete");
    }

    @Override
    public String getStateName() { return "REJECTED"; }
}
