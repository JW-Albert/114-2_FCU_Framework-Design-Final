package com.vehicle.management.domain.state;

import com.vehicle.management.domain.model.BorrowingRequest;

/**
 * 使用中狀態（ConcreteState — IN_USE）。
 *
 * <p>車輛已出車，正在使用中。
 * 只允許還車操作（{@link #complete}，→ {@link ReturnedState}）；
 * 其他操作皆拋出 {@link InvalidStateTransitionException}。</p>
 */
public class InUseState implements BorrowingState {

    /** @throws InvalidStateTransitionException 使用中不可核准 */
    @Override
    public void approve(BorrowingRequest request, String reviewNote) {
        throw new InvalidStateTransitionException(getStateName(), "approve");
    }

    /** @throws InvalidStateTransitionException 使用中不可拒絕 */
    @Override
    public void reject(BorrowingRequest request, String reviewNote) {
        throw new InvalidStateTransitionException(getStateName(), "reject");
    }

    /** @throws InvalidStateTransitionException 已在使用中，不可再次出車 */
    @Override
    public void startUse(BorrowingRequest request) {
        throw new InvalidStateTransitionException(getStateName(), "startUse");
    }

    /**
     * 完成用車並還車，狀態轉換為 {@link ReturnedState}。
     *
     * @param request 借車申請物件
     */
    @Override
    public void complete(BorrowingRequest request) {
        request.transitionState(new ReturnedState(), null);
    }

    /** @return {@code "IN_USE"} */
    @Override
    public String getStateName() { return "IN_USE"; }
}
