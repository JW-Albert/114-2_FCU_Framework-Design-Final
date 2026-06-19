package com.vehicle.management.domain.state;

import com.vehicle.management.domain.model.BorrowingRequest;

/**
 * 借車申請狀態介面（State Pattern，Ch13）。
 *
 * <p><b>設計動機：</b>借車申請有五個生命週期狀態（PENDING → APPROVED → IN_USE → RETURNED，
 * 或 PENDING → REJECTED），各狀態允許的操作不同。
 * 若在 {@link BorrowingRequest} 中以 {@code if/switch} 判斷，隨狀態增加程式將難以維護。
 * State Pattern 將每個狀態的行為封裝為獨立類別，新增狀態只需新增實作，
 * 符合開放封閉原則（OCP）。</p>
 *
 * <p><b>合法轉換對照表：</b>
 * <pre>
 *   狀態         | approve | reject | startUse | complete
 *   -------------|---------|--------|----------|----------
 *   PENDING      |   ✓     |   ✓   |   ✗      |   ✗
 *   APPROVED     |   ✗     |   ✗   |   ✓      |   ✗
 *   IN_USE       |   ✗     |   ✗   |   ✗      |   ✓
 *   RETURNED     |   ✗     |   ✗   |   ✗      |   ✗
 *   REJECTED     |   ✗     |   ✗   |   ✗      |   ✗
 * </pre>
 * ✗ 表示拋出 {@link InvalidStateTransitionException}。</p>
 *
 * @see BorrowingRequest#transitionState(BorrowingState, String)
 */
public interface BorrowingState {

    /**
     * 核准借車申請，並記錄審核備註。
     *
     * @param request    借車申請物件（用於呼叫 {@link BorrowingRequest#transitionState}）
     * @param reviewNote 管理員核准備註
     * @throws InvalidStateTransitionException 若目前狀態不允許核准
     */
    void approve(BorrowingRequest request, String reviewNote);

    /**
     * 拒絕借車申請，並記錄拒絕原因。
     *
     * @param request    借車申請物件
     * @param reviewNote 管理員拒絕原因
     * @throws InvalidStateTransitionException 若目前狀態不允許拒絕
     */
    void reject(BorrowingRequest request, String reviewNote);

    /**
     * 執行出車（將申請標記為使用中）。
     *
     * @param request 借車申請物件
     * @throws InvalidStateTransitionException 若目前狀態不允許出車
     */
    void startUse(BorrowingRequest request);

    /**
     * 完成用車並還車（將申請標記為已完成）。
     *
     * @param request 借車申請物件
     * @throws InvalidStateTransitionException 若目前狀態不允許還車
     */
    void complete(BorrowingRequest request);

    /**
     * 撤銷核准，將已核准的申請退回待審核（僅 {@link ApprovedState} 允許）。
     *
     * @param request 借車申請物件
     * @param note    撤銷原因
     * @throws InvalidStateTransitionException 若目前狀態不允許撤銷
     */
    void revoke(BorrowingRequest request, String note);

    /**
     * 取得此狀態的名稱字串。
     *
     * @return 狀態名稱，例如 {@code "PENDING"}、{@code "APPROVED"}
     */
    String getStateName();
}
