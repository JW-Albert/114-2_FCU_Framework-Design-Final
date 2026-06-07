package com.vehicle.management.repository.inmemory;

import com.vehicle.management.domain.model.MaintenanceRecord;
import com.vehicle.management.repository.IMaintenanceRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryMaintenanceRepository implements IMaintenanceRepository {

    private final Map<UUID, MaintenanceRecord> store = new ConcurrentHashMap<>();

    @Override
    public Optional<MaintenanceRecord> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<MaintenanceRecord> findByVehicleId(UUID vehicleId) {
        return store.values().stream()
                .filter(r -> vehicleId.equals(r.getVehicleId()))
                .toList();
    }

    @Override
    public List<MaintenanceRecord> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public MaintenanceRecord save(MaintenanceRecord record) {
        store.put(record.getId(), record);
        return record;
    }

    @Override
    public void delete(UUID id) {
        store.remove(id);
    }
}
