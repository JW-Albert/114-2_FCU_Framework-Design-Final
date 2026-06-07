package com.vehicle.management.infrastructure.persistence;

import com.vehicle.management.domain.model.MaintenanceRecord;
import com.vehicle.management.infrastructure.persistence.entity.MaintenanceRecordEntity;
import com.vehicle.management.infrastructure.persistence.jpa.JpaMaintenanceRepo;
import com.vehicle.management.repository.IMaintenanceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Adapter (Ch14) */
@Repository
public class MaintenanceRepositoryAdapter implements IMaintenanceRepository {

    private final JpaMaintenanceRepo jpa;

    public MaintenanceRepositoryAdapter(JpaMaintenanceRepo jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<MaintenanceRecord> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<MaintenanceRecord> findByVehicleId(UUID vehicleId) {
        return jpa.findByVehicleId(vehicleId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<MaintenanceRecord> findAll() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public MaintenanceRecord save(MaintenanceRecord record) {
        return toDomain(jpa.save(toEntity(record)));
    }

    @Override
    public void delete(UUID id) {
        jpa.deleteById(id);
    }

    private MaintenanceRecord toDomain(MaintenanceRecordEntity e) {
        return new MaintenanceRecord(e.getId(), e.getVehicleId(), e.getDate(),
                e.getItems(), e.getCost(), e.getNextDueDate(), e.getNextDueKm(), e.getCreatedAt());
    }

    private MaintenanceRecordEntity toEntity(MaintenanceRecord r) {
        MaintenanceRecordEntity e = new MaintenanceRecordEntity();
        e.setId(r.getId());
        e.setVehicleId(r.getVehicleId());
        e.setDate(r.getDate());
        e.setItems(r.getItems());
        e.setCost(r.getCost());
        e.setNextDueDate(r.getNextDueDate());
        e.setNextDueKm(r.getNextDueKm());
        e.setCreatedAt(r.getCreatedAt());
        return e;
    }
}
