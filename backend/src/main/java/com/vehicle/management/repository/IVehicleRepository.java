package com.vehicle.management.repository;

import com.vehicle.management.domain.model.Vehicle;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** DIP: Service 層依賴此介面，不依賴具體 DB 實作。 */
public interface IVehicleRepository {
    Optional<Vehicle> findById(UUID id);
    List<Vehicle> findAll();
    List<Vehicle> findAvailable(Instant periodStart, Instant periodEnd);
    Vehicle save(Vehicle vehicle);
    void delete(UUID id);
}
