package com.vehicle.management.repository;

import com.vehicle.management.domain.model.MaintenanceRecord;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IMaintenanceRepository {
    Optional<MaintenanceRecord> findById(UUID id);
    List<MaintenanceRecord> findByVehicleId(UUID vehicleId);
    List<MaintenanceRecord> findAll();
    MaintenanceRecord save(MaintenanceRecord record);
    void delete(UUID id);
}
