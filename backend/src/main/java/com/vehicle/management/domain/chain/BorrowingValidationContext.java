package com.vehicle.management.domain.chain;

import com.vehicle.management.domain.model.User;

import java.time.Instant;
import java.util.UUID;

/**
 * 借車申請驗證的上下文資料（Chain of Responsibility Pattern, GoF Ch14）。
 *
 * <p>封裝一次 {@code submitRequest} 呼叫所需的所有輸入，傳遞給責任鏈中每個 {@link BorrowingValidator}。</p>
 */
public record BorrowingValidationContext(
        User actor,
        UUID vehicleId,
        Instant periodStart,
        Instant periodEnd
) {}
