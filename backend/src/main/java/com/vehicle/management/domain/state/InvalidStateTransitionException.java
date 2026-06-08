package com.vehicle.management.domain.state;

/**
 * 非法狀態轉換例外。
 *
 * <p>當 {@link BorrowingState} 的實作方法被不合法地呼叫時拋出此例外，
 * 例如對已完成的申請再次核准。
 * 此例外屬於 {@link RuntimeException}（非受檢例外），
 * 表示呼叫端的程式邏輯有誤，應在 API 層轉換為 HTTP 400 Bad Request 回應。</p>
 */
public class InvalidStateTransitionException extends RuntimeException {

    /**
     * 建構包含描述性訊息的例外物件。
     *
     * @param currentState 目前的狀態名稱（例如 {@code "RETURNED"}）
     * @param operation    被非法呼叫的操作名稱（例如 {@code "approve"}）
     */
    public InvalidStateTransitionException(String currentState, String operation) {
        super("Cannot perform '" + operation + "' on a request in state '" + currentState + "'");
    }
}
