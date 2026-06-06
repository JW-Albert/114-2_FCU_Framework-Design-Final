package com.vehicle.management.domain.model;

interface BorrowingState {
    void approve(BorrowingRequest request, String reviewNote);
    void reject(BorrowingRequest request, String reviewNote);
    void startUse(BorrowingRequest request);
    void complete(BorrowingRequest request);
    String getStateName();
}
