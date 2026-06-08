package com.vehicle.management.domain.observer;

import com.vehicle.management.domain.model.BorrowingRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * 借車事件發布者（Observer Pattern Subject，Ch20）。
 *
 * <p>{@link com.vehicle.management.service.BorrowingService} 繼承此抽象類別，
 * 在借車申請狀態改變後呼叫 {@code notify*} 方法廣播事件給所有已註冊的
 * {@link BorrowingEventObserver}。</p>
 *
 * <p><b>設計優點：</b>
 * <ul>
 *   <li><b>DIP</b>：發布者只依賴 {@link BorrowingEventObserver} 介面，不感知具體通知實作。</li>
 *   <li><b>OCP</b>：新增觀察者（例如 SMS 通知）無需修改此類別或 Service 層。</li>
 *   <li><b>SRP</b>：觀察者管理（add/remove/notify）邏輯集中在此，不污染業務邏輯。</li>
 * </ul>
 */
public abstract class BorrowingEventPublisher {

    /** 已註冊的觀察者清單。 */
    private final List<BorrowingEventObserver> observers = new ArrayList<>();

    /**
     * 向發布者註冊一個觀察者。
     *
     * @param observer 欲訂閱事件的觀察者，不可為 {@code null}
     */
    public void addObserver(BorrowingEventObserver observer) {
        observers.add(observer);
    }

    /**
     * 從發布者移除已註冊的觀察者。
     *
     * @param observer 欲取消訂閱的觀察者
     */
    public void removeObserver(BorrowingEventObserver observer) {
        observers.remove(observer);
    }

    /**
     * 廣播「申請核准」事件給所有觀察者。
     *
     * @param request 剛被核准的借車申請
     */
    protected void notifyApproved(BorrowingRequest request) {
        observers.forEach(o -> o.onApproved(request));
    }

    /**
     * 廣播「申請拒絕」事件給所有觀察者。
     *
     * @param request 剛被拒絕的借車申請
     */
    protected void notifyRejected(BorrowingRequest request) {
        observers.forEach(o -> o.onRejected(request));
    }

    /**
     * 廣播「車輛出車」事件給所有觀察者。
     *
     * @param request 剛出車的借車申請
     */
    protected void notifyStarted(BorrowingRequest request) {
        observers.forEach(o -> o.onStarted(request));
    }

    /**
     * 廣播「車輛還車」事件給所有觀察者。
     *
     * @param request 剛完成的借車申請
     */
    protected void notifyCompleted(BorrowingRequest request) {
        observers.forEach(o -> o.onCompleted(request));
    }
}
