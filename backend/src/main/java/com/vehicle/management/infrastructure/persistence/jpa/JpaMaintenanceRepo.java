package com.vehicle.management.infrastructure.persistence.jpa;

import com.vehicle.management.infrastructure.persistence.entity.MaintenanceRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaMaintenanceRepo extends JpaRepository<MaintenanceRecordEntity, UUID> {
    List<MaintenanceRecordEntity> findByVehicleId(UUID vehicleId);
}
