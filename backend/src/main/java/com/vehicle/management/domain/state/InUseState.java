package com.vehicle.management.domain.state;

import com.vehicle.management.domain.model.BorrowingRequest;

public class InUseState implements BorrowingState {

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
        throw new InvalidStateTransitionException(getStateName(), "startUse");
    }

    @Override
    public void complete(BorrowingRequest request) {
        request.setState(new ReturnedState());
    }

    @Override
    public String getStateName() { return "IN_USE"; }
}
