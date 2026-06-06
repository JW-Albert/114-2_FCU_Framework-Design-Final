package com.vehicle.management.domain.state;

import com.vehicle.management.domain.model.BorrowingRequest;

public class PendingState implements BorrowingState {

    @Override
    public void approve(BorrowingRequest request, String reviewNote) {
        request.setReviewNote(reviewNote);
        request.setState(new ApprovedState());
    }

    @Override
    public void reject(BorrowingRequest request, String reviewNote) {
        request.setReviewNote(reviewNote);
        request.setState(new RejectedState());
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
