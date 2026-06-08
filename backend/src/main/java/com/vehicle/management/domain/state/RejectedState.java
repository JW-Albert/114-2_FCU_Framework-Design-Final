package com.vehicle.management.domain.state;

import com.vehicle.management.domain.model.BorrowingRequest;

/**
 * 已拒絕狀態（ConcreteState — REJECTED）。
 *
 * <p>申請流程的終態之一（管理員拒絕）。
 * 此狀態下所有操作皆不合法，呼叫任何方法均拋出 {@link InvalidStateTransitionException}。</p>
 */
public class RejectedState implements BorrowingState {

    /** @throws InvalidStateTransitionException 已拒絕的申請不可再操作 */
    @Override
    public void approve(BorrowingRequest request, String reviewNote) {
        throw new InvalidStateTransitionException(getStateName(), "approve");
    }

    /** @throws InvalidStateTransitionException 已拒絕的申請不可再操作 */
    @Override
    public void reject(BorrowingRequest request, String reviewNote) {
        throw new InvalidStateTransitionException(getStateName(), "reject");
    }

    /** @throws InvalidStateTransitionException 已拒絕的申請不可再操作 */
    @Override
    public void startUse(BorrowingRequest request) {
        throw new InvalidStateTransitionException(getStateName(), "startUse");
    }

    /** @throws InvalidStateTransitionException 已拒絕的申請不可再操作 */
    @Override
    public void complete(BorrowingRequest request) {
        throw new InvalidStateTransitionException(getStateName(), "complete");
    }

    /** @return {@code "REJECTED"} */
    @Override
    public String getStateName() { return "REJECTED"; }
}
