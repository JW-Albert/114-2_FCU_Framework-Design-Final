package com.vehicle.management.domain.state;

import com.vehicle.management.domain.model.BorrowingRequest;

/**
 * 已還車狀態（ConcreteState — RETURNED）。
 *
 * <p>申請流程的終態之一（成功完成）。
 * 此狀態下所有操作皆不合法，呼叫任何方法均拋出 {@link InvalidStateTransitionException}。</p>
 */
public class ReturnedState implements BorrowingState {

    /** @throws InvalidStateTransitionException 已完成的申請不可再操作 */
    @Override
    public void approve(BorrowingRequest request, String reviewNote) {
        throw new InvalidStateTransitionException(getStateName(), "approve");
    }

    /** @throws InvalidStateTransitionException 已完成的申請不可再操作 */
    @Override
    public void reject(BorrowingRequest request, String reviewNote) {
        throw new InvalidStateTransitionException(getStateName(), "reject");
    }

    /** @throws InvalidStateTransitionException 已完成的申請不可再操作 */
    @Override
    public void startUse(BorrowingRequest request) {
        throw new InvalidStateTransitionException(getStateName(), "startUse");
    }

    /** @throws InvalidStateTransitionException 已完成的申請不可再操作 */
    @Override
    public void complete(BorrowingRequest request) {
        throw new InvalidStateTransitionException(getStateName(), "complete");
    }

    /** @return {@code "RETURNED"} */
    @Override
    public String getStateName() { return "RETURNED"; }
}
