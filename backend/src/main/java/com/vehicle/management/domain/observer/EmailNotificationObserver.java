package com.vehicle.management.domain.observer;

import com.vehicle.management.domain.model.BorrowingRequest;

/**
 * ConcreteObserver (Ch20): Email 通知實作。
 * 替換或新增通知方式（SMS、推播）只需實作 BorrowingEventObserver，
 * 不需修改 BorrowingService（OCP）。
 */
public class EmailNotificationObserver implements BorrowingEventObserver {

    @Override
    public void onApproved(BorrowingRequest request) {
        System.out.printf("[Email] 借車申請 %s 已核准，請於指定時段取車。%n",
                request.getId());
    }

    @Override
    public void onRejected(BorrowingRequest request) {
        System.out.printf("[Email] 借車申請 %s 已拒絕，原因：%s%n",
                request.getId(), request.getReviewNote());
    }

    @Override
    public void onStarted(BorrowingRequest request) {
        System.out.printf("[Email] 車輛已出車，申請單 %s。%n", request.getId());
    }

    @Override
    public void onCompleted(BorrowingRequest request) {
        System.out.printf("[Email] 車輛已還車，申請單 %s 已完成。%n", request.getId());
    }
}
