package com.vehicle.management.domain.observer;

import com.vehicle.management.domain.model.BorrowingRequest;

/**
 * Email 通知觀察者（ConcreteObserver，Observer Pattern Ch20）。
 *
 * <p>目前以標準輸出模擬 Email 發送，實際部署時可替換為呼叫
 * Spring Mail 或第三方 Email API，無需修改其他程式碼（OCP）。</p>
 *
 * <p>替換或新增其他通知管道（SMS、推播通知）只需實作 {@link BorrowingEventObserver}
 * 並向 {@link BorrowingEventPublisher#addObserver} 註冊即可。</p>
 */
public class EmailNotificationObserver implements BorrowingEventObserver {

    /**
     * 申請核准通知。
     *
     * @param request 已核准的借車申請
     */
    @Override
    public void onApproved(BorrowingRequest request) {
        System.out.printf("[Email] 借車申請 %s 已核准，請於指定時段取車。%n",
                request.getId());
    }

    /**
     * 申請拒絕通知，附帶拒絕原因。
     *
     * @param request 已拒絕的借車申請
     */
    @Override
    public void onRejected(BorrowingRequest request) {
        System.out.printf("[Email] 借車申請 %s 已拒絕，原因：%s%n",
                request.getId(), request.getReviewNote());
    }

    /**
     * 出車通知。
     *
     * @param request 已出車的借車申請
     */
    @Override
    public void onStarted(BorrowingRequest request) {
        System.out.printf("[Email] 車輛已出車，申請單 %s。%n", request.getId());
    }

    /**
     * 還車完成通知。
     *
     * @param request 已完成的借車申請
     */
    @Override
    public void onCompleted(BorrowingRequest request) {
        System.out.printf("[Email] 車輛已還車，申請單 %s 已完成。%n", request.getId());
    }
}
