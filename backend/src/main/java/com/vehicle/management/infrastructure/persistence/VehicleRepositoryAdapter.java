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
        return jpa.findById(id).filter(e -> e.getDeletedAt() == null).map(this::toDomain);
    }

    @Override
    public List<Vehicle> findAll() {
        return jpa.findByDeletedAtIsNull().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Vehicle> findAvailable(Instant start, Instant end) {
        return jpa.findAvailable(start, end).stream()
                .filter(e -> e.getDeletedAt() == null)
                .map(this::toDomain).toList();
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        VehicleEntity entity = toEntity(vehicle);
        return toDomain(jpa.save(entity));
    }

    @Override
    public void delete(UUID id) {
        // 軟刪除：標記 deletedAt 而非實際移除
        jpa.findById(id).ifPresent(e -> {
            e.setDeletedAt(Instant.now());
            jpa.save(e);
        });
    }

    @Override
    public void restore(UUID id) {
        jpa.findById(id).ifPresent(e -> {
            e.setDeletedAt(null);
            jpa.save(e);
        });
    }

    @Override
    public List<Vehicle> findDeleted() {
        return jpa.findByDeletedAtIsNotNull().stream().map(this::toDomain).toList();
    }

    private Vehicle toDomain(VehicleEntity e) {
        Vehicle v = new Vehicle(e.getId(), e.getPlate(), e.getModel(), e.getYear(),
                VehicleStatus.valueOf(e.getStatus()), e.getCreatedAt(), e.getCurrentMileage());
        v.setDeletedAt(e.getDeletedAt());
        return v;
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
        e.setDeletedAt(v.getDeletedAt());
        return e;
    }
}
