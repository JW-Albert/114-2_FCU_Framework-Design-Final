package com.vehicle.management.domain.state;

import com.vehicle.management.domain.model.BorrowingRequest;

/**
 * 已核准狀態（ConcreteState — APPROVED）。
 *
 * <p>管理員已審核通過，等待出車。
 * 只允許出車操作（{@link #startUse}，→ {@link InUseState}）；
 * 其他操作皆拋出 {@link InvalidStateTransitionException}。</p>
 */
public class ApprovedState implements BorrowingState {

    /** @throws InvalidStateTransitionException 已核准的申請不可再次核准 */
    @Override
    public void approve(BorrowingRequest request, String reviewNote) {
        throw new InvalidStateTransitionException(getStateName(), "approve");
    }

    /** @throws InvalidStateTransitionException 已核准的申請不可拒絕 */
    @Override
    public void reject(BorrowingRequest request, String reviewNote) {
        throw new InvalidStateTransitionException(getStateName(), "reject");
    }

    /**
     * 執行出車，狀態轉換為 {@link InUseState}。
     *
     * @param request 借車申請物件
     */
    @Override
    public void startUse(BorrowingRequest request) {
        request.transitionState(new InUseState(), null);
    }

    /** @throws InvalidStateTransitionException 尚未出車，無法還車 */
    @Override
    public void complete(BorrowingRequest request) {
        throw new InvalidStateTransitionException(getStateName(), "complete");
    }

    /** @return {@code "APPROVED"} */
    @Override
    public String getStateName() { return "APPROVED"; }
}
