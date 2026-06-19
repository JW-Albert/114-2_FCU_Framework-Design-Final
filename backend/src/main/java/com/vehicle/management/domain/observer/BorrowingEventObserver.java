package com.vehicle.management.domain.observer;

import com.vehicle.management.domain.model.BorrowingRequest;

/**
 * 借車事件觀察者介面（Observer Pattern，Ch20）。
 *
 * <p>實作此介面以訂閱借車申請的生命週期事件。
 * 目前提供的事件包含：核准、拒絕、出車、還車。</p>
 *
 * <p><b>OCP 應用：</b>新增通知方式（例如 SMS、推播）只需新增實作類別，
 * 並向 {@link BorrowingEventPublisher#addObserver} 註冊，
 * 無需修改 {@link com.vehicle.management.service.BorrowingService} 或此介面。</p>
 *
 * @see BorrowingEventPublisher
 * @see EmailNotificationObserver
 */
public interface BorrowingEventObserver {

    /**
     * 借車申請被送出時觸發（進入 PENDING 狀態，等待審核）。
     *
     * @param request 剛送出的借車申請
     */
    void onSubmitted(BorrowingRequest request);

    /**
     * 借車申請被核准時觸發。
     *
     * @param request 已核准的借車申請
     */
    void onApproved(BorrowingRequest request);

    /**
     * 借車申請被拒絕時觸發。
     *
     * @param request 已拒絕的借車申請
     */
    void onRejected(BorrowingRequest request);

    /**
     * 車輛出車時觸發（申請進入 IN_USE 狀態）。
     *
     * @param request 已出車的借車申請
     */
    void onStarted(BorrowingRequest request);

    /**
     * 車輛還車時觸發（申請進入 RETURNED 狀態）。
     *
     * @param request 已完成的借車申請
     */
    void onCompleted(BorrowingRequest request);
}
