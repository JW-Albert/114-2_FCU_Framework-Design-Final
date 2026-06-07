package com.vehicle.management.repository.inmemory;

import com.vehicle.management.domain.model.BorrowingRequest;
import com.vehicle.management.domain.model.Vehicle;
import com.vehicle.management.domain.model.VehicleStatus;
import com.vehicle.management.repository.IVehicleRepository;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DIP: 測試環境注入此實作，無需資料庫連線，測試毫秒完成。
 */
public class InMemoryVehicleRepository implements IVehicleRepository {

    private final Map<UUID, Vehicle> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Vehicle> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Vehicle> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public List<Vehicle> findAvailable(Instant periodStart, Instant periodEnd) {
        return store.values().stream()
                .filter(v -> v.getStatus() == VehicleStatus.AVAILABLE)
                .toList();
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        store.put(vehicle.getId(), vehicle);
        return vehicle;
    }

    @Override
    public void delete(UUID id) {
        store.remove(id);
    }
}
