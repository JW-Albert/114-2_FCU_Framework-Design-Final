package com.vehicle.management.domain.observer;

import com.vehicle.management.domain.model.BorrowingRequest;

/**
 * Observer (Ch20): 借車申請事件的觀察者介面。
 * 實作此介面以接收狀態變更通知（審核結果、出車、還車）。
 */
public interface BorrowingEventObserver {
    void onApproved(BorrowingRequest request);
    void onRejected(BorrowingRequest request);
    void onStarted(BorrowingRequest request);
    void onCompleted(BorrowingRequest request);
}
