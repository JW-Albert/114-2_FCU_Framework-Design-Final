package com.vehicle.management.domain.chain;

/**
 * 借車申請驗證器介面（Handler，Chain of Responsibility Pattern, GoF Ch14）。
 *
 * <p>實作類別組成一條有序責任鏈，依序對 {@link BorrowingValidationContext} 進行驗證。
 * 任何驗證器丟出例外即中斷後續鏈節。新增驗證規則只需新增一個實作並加上 {@code @Order}，
 * 無需修改 {@link com.vehicle.management.service.BorrowingService}（OCP）。</p>
 */
public interface BorrowingValidator {
    /**
     * 驗證借車申請是否符合此節點的規則。
     *
     * @param ctx 驗證上下文（申請人、車輛、時段）
     * @throws com.vehicle.management.service.PermissionDeniedException 若申請人權限不足
     * @throws com.vehicle.management.service.ResourceNotFoundException  若車輛不存在
     * @throws com.vehicle.management.service.ConflictException          若時段衝突
     */
    void validate(BorrowingValidationContext ctx);
}
