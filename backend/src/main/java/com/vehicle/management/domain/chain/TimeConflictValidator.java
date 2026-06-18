package com.vehicle.management.domain.chain;

import com.vehicle.management.domain.model.BorrowingRequest;
import com.vehicle.management.domain.strategy.ConflictCheckStrategy;
import com.vehicle.management.repository.IBorrowingRepository;
import com.vehicle.management.service.ConflictException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 第三個責任鏈節點：以 {@link ConflictCheckStrategy} 驗證時段是否與現有借車申請衝突。
 *
 * <p>Strategy Pattern 與 Chain of Responsibility 的組合：衝突演算法可替換，驗證順序由鏈節點 {@code @Order} 控制。</p>
 */
@Component
@Order(3)
public class TimeConflictValidator implements BorrowingValidator {

    private final IBorrowingRepository borrowingRepo;
    private final ConflictCheckStrategy conflictStrategy;

    public TimeConflictValidator(IBorrowingRepository borrowingRepo,
                                 ConflictCheckStrategy conflictStrategy) {
        this.borrowingRepo = borrowingRepo;
        this.conflictStrategy = conflictStrategy;
    }

    @Override
    public void validate(BorrowingValidationContext ctx) {
        List<BorrowingRequest> conflicts =
                borrowingRepo.findConflicting(ctx.vehicleId(), ctx.periodStart(), ctx.periodEnd());
        if (conflictStrategy.hasConflict(conflicts, ctx.periodStart(), ctx.periodEnd())) {
            throw new ConflictException("Vehicle is already booked for this period");
        }
    }
}
