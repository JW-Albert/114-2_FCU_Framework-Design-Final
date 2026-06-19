package com.vehicle.management.domain.state;

import com.vehicle.management.domain.model.BorrowingRequest;

/**
 * 待審核狀態（ConcreteState — PENDING）。
 *
 * <p>申請建立後的初始狀態。管理員可核准（→ {@link ApprovedState}）
 * 或拒絕（→ {@link RejectedState}）；
 * 其他操作（出車、還車）不合法，呼叫時拋出 {@link InvalidStateTransitionException}。</p>
 */
public class PendingState implements BorrowingState {

    /**
     * 核准申請，狀態轉換為 {@link ApprovedState}。
     *
     * @param request    借車申請物件
     * @param reviewNote 管理員核准備註
     */
    @Override
    public void approve(BorrowingRequest request, String reviewNote) {
        request.transitionState(new ApprovedState(), reviewNote);
    }

    /**
     * 拒絕申請，狀態轉換為 {@link RejectedState}。
     *
     * @param request    借車申請物件
     * @param reviewNote 管理員拒絕原因
     */
    @Override
    public void reject(BorrowingRequest request, String reviewNote) {
        request.transitionState(new RejectedState(), reviewNote);
    }

    /** @throws InvalidStateTransitionException 待審核狀態不允許出車 */
    @Override
    public void startUse(BorrowingRequest request) {
        throw new InvalidStateTransitionException(getStateName(), "startUse");
    }

    /** @throws InvalidStateTransitionException 待審核狀態不允許還車 */
    @Override
    public void complete(BorrowingRequest request) {
        throw new InvalidStateTransitionException(getStateName(), "complete");
    }

    /** @throws InvalidStateTransitionException 尚未核准，無核准可撤銷 */
    @Override
    public void revoke(BorrowingRequest request, String note) {
        throw new InvalidStateTransitionException(getStateName(), "revoke");
    }

    /** @return {@code "PENDING"} */
    @Override
    public String getStateName() { return "PENDING"; }
}
