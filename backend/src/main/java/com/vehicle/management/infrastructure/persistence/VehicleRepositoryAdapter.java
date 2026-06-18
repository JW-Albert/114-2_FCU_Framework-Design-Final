package com.vehicle.management.infrastructure.persistence;

import com.vehicle.management.domain.model.Vehicle;
import com.vehicle.management.domain.model.VehicleStatus;
import com.vehicle.management.infrastructure.persistence.entity.VehicleEntity;
import com.vehicle.management.infrastructure.persistence.jpa.JpaVehicleRepo;
import com.vehicle.management.repository.IVehicleRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter (Ch14): 將 Spring Data JPA 介面轉換成 Domain 所需的 IVehicleRepository 介面。
 * Service 層只看到 IVehicleRepository，不感知 JPA 存在（DIP）。
 */
@Repository
public class VehicleRepositoryAdapter implements IVehicleRepository {

    private final JpaVehicleRepo jpa;

    public VehicleRepositoryAdapter(JpaVehicleRepo jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Vehicle> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Vehicle> findAll() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Vehicle> findAvailable(Instant start, Instant end) {
        return jpa.findAvailable(start, end).stream().map(this::toDomain).toList();
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        VehicleEntity entity = toEntity(vehicle);
        return toDomain(jpa.save(entity));
    }

    @Override
    public void delete(UUID id) {
        jpa.deleteById(id);
    }

    private Vehicle toDomain(VehicleEntity e) {
        return new Vehicle(e.getId(), e.getPlate(), e.getModel(), e.getYear(),
                VehicleStatus.valueOf(e.getStatus()), e.getCreatedAt(), e.getCurrentMileage());
    }

    private VehicleEntity toEntity(Vehicle v) {
        VehicleEntity e = new VehicleEntity();
        e.setId(v.getId());
        e.setPlate(v.getPlate());
        e.setModel(v.getModel());
        e.setYear(v.getYear());
        e.setStatus(v.getStatus().name());
        e.setCreatedAt(v.getCreatedAt());
        e.setCurrentMileage(v.getCurrentMileage());
        return e;
    }
}
