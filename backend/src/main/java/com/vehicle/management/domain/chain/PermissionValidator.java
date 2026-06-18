package com.vehicle.management.domain.chain;

import com.vehicle.management.domain.role.Permission;
import com.vehicle.management.service.PermissionDeniedException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 第一個責任鏈節點：驗證申請人是否具備提交借車申請的權限。
 */
@Component
@Order(1)
public class PermissionValidator implements BorrowingValidator {

    @Override
    public void validate(BorrowingValidationContext ctx) {
        if (!ctx.actor().can(Permission.SUBMIT_REQUEST)) {
            throw new PermissionDeniedException(ctx.actor().getEmail() + " cannot submit requests");
        }
    }
}
