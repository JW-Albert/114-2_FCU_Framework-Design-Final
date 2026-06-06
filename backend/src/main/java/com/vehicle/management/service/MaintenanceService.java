package com.vehicle.management.service;

import com.vehicle.management.domain.model.MaintenanceRecord;
import com.vehicle.management.domain.model.User;
import com.vehicle.management.domain.role.Permission;
import com.vehicle.management.repository.IMaintenanceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SRP: 只負責保養紀錄管理與到期提醒。
 * DIP: 依賴 IMaintenanceRepository 介面。
 */
@Service
public class MaintenanceService {

    private final IMaintenanceRepository maintenanceRepo;

    public MaintenanceService(IMaintenanceRepository maintenanceRepo) {
        this.maintenanceRepo = maintenanceRepo;
    }

    public MaintenanceRecord addRecord(User actor, UUID vehicleId, LocalDate date,
                                       List<String> items, BigDecimal cost,
                                       LocalDate nextDueDate, Integer nextDueKm) {
        if (!actor.can(Permission.MANAGE_VEHICLE)) {
            throw new PermissionDeniedException(actor.getEmail() + " cannot manage vehicles");
        }
        MaintenanceRecord record = new MaintenanceRecord(
                UUID.randomUUID(), vehicleId, date, items, cost,
                nextDueDate, nextDueKm, Instant.now());
        return maintenanceRepo.save(record);
    }

    public List<MaintenanceRecord> getRecords(UUID vehicleId) {
        return maintenanceRepo.findByVehicleId(vehicleId);
    }

    public List<MaintenanceRecord> getDueReminders(LocalDate currentDate,
                                                    Map<UUID, Integer> currentKmMap) {
        return maintenanceRepo.findAll().stream()
                .filter(r -> r.isDue(currentDate,
                        currentKmMap.getOrDefault(r.getVehicleId(), 0)))
                .toList();
    }

    public void deleteRecord(User actor, UUID recordId) {
        if (!actor.can(Permission.MANAGE_VEHICLE)) {
            throw new PermissionDeniedException(actor.getEmail() + " cannot manage vehicles");
        }
        maintenanceRepo.delete(recordId);
    }
}
