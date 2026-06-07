package com.vehicle.management.service;

import com.vehicle.management.domain.model.User;
import com.vehicle.management.domain.model.Vehicle;
import com.vehicle.management.domain.model.VehicleStatus;
import com.vehicle.management.domain.role.Permission;
import com.vehicle.management.repository.IVehicleRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * SRP: 只負責車輛資產的 CRUD 與狀態管理。
 * DIP: 依賴 IVehicleRepository 介面，不感知具體 DB 實作。
 */
@Service
public class VehicleService {

    private final IVehicleRepository vehicleRepo;

    public VehicleService(IVehicleRepository vehicleRepo) {
        this.vehicleRepo = vehicleRepo;
    }

    public Vehicle createVehicle(User actor, String plate, String model, int year) {
        checkPermission(actor, Permission.MANAGE_VEHICLE);
        Vehicle vehicle = new Vehicle(UUID.randomUUID(), plate, model, year,
                VehicleStatus.AVAILABLE, Instant.now());
        return vehicleRepo.save(vehicle);
    }

    public Vehicle getVehicle(UUID id) {
        return vehicleRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + id));
    }

    public List<Vehicle> listAll() {
        return vehicleRepo.findAll();
    }

    public List<Vehicle> findAvailable(Instant start, Instant end) {
        return vehicleRepo.findAvailable(start, end);
    }

    public Vehicle updateVehicle(User actor, UUID id, String plate, String model, int year) {
        checkPermission(actor, Permission.MANAGE_VEHICLE);
        Vehicle v = getVehicle(id);
        Vehicle updated = new Vehicle(v.getId(), plate, model, year, v.getStatus(), v.getCreatedAt());
        return vehicleRepo.save(updated);
    }

    public void deleteVehicle(User actor, UUID id) {
        checkPermission(actor, Permission.MANAGE_VEHICLE);
        vehicleRepo.delete(id);
    }

    private void checkPermission(User actor, Permission permission) {
        if (!actor.can(permission)) {
            throw new PermissionDeniedException(
                    actor.getEmail() + " lacks permission: " + permission);
        }
    }
}
