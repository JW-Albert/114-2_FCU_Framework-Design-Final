package com.vehicle.management.domain.observer;

import com.vehicle.management.domain.model.BorrowingRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Observer (Ch20) - Subject/Observable:
 * BorrowingService 繼承此類別，當借車申請狀態改變時，
 * 廣播通知所有已註冊的 Observer，無需與具體通知實作耦合（DIP）。
 */
public abstract class BorrowingEventPublisher {

    private final List<BorrowingEventObserver> observers = new ArrayList<>();

    public void addObserver(BorrowingEventObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(BorrowingEventObserver observer) {
        observers.remove(observer);
    }

    protected void notifyApproved(BorrowingRequest request) {
        observers.forEach(o -> o.onApproved(request));
    }

    protected void notifyRejected(BorrowingRequest request) {
        observers.forEach(o -> o.onRejected(request));
    }

    protected void notifyStarted(BorrowingRequest request) {
        observers.forEach(o -> o.onStarted(request));
    }

    protected void notifyCompleted(BorrowingRequest request) {
        observers.forEach(o -> o.onCompleted(request));
    }
}
