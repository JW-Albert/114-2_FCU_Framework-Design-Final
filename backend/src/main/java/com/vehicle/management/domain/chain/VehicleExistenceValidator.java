package com.vehicle.management.domain.chain;

import com.vehicle.management.repository.IVehicleRepository;
import com.vehicle.management.service.ResourceNotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 第二個責任鏈節點：驗證申請的目標車輛是否存在。
 */
@Component
@Order(2)
public class VehicleExistenceValidator implements BorrowingValidator {

    private final IVehicleRepository vehicleRepo;

    public VehicleExistenceValidator(IVehicleRepository vehicleRepo) {
        this.vehicleRepo = vehicleRepo;
    }

    @Override
    public void validate(BorrowingValidationContext ctx) {
        vehicleRepo.findById(ctx.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + ctx.vehicleId()));
    }
}
