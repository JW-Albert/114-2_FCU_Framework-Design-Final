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
 * 保養紀錄管理業務邏輯服務（Template Method Pattern）。
 *
 * <p><b>套用的設計原則：</b>
 * <ul>
 *   <li><b>SRP</b>：只負責保養紀錄的新增、查詢、刪除，以及到期提醒掃描。</li>
 *   <li><b>DIP</b>：依賴 {@link IMaintenanceRepository} 介面，不感知具體 DB 實作。</li>
 *   <li><b>Template Method（Ch7）</b>：繼承 {@link AbstractProtectedService}，
 *       權限驗證骨架由父類別統一定義（消除 if-throw 重複邏輯）。</li>
 * </ul>
 */
@Service
public class MaintenanceService extends AbstractProtectedService {

    private final IMaintenanceRepository maintenanceRepo;

    public MaintenanceService(IMaintenanceRepository maintenanceRepo) {
        this.maintenanceRepo = maintenanceRepo;
    }

    /**
     * 新增保養紀錄。
     *
     * @throws PermissionDeniedException 若使用者不具備 {@link Permission#MANAGE_VEHICLE}
     */
    public MaintenanceRecord addRecord(User actor, UUID vehicleId, LocalDate date,
                                       List<String> items, BigDecimal cost,
                                       LocalDate nextDueDate, Integer nextDueKm) {
        return supply(actor, Permission.MANAGE_VEHICLE, () -> {
            MaintenanceRecord record = new MaintenanceRecord(
                    UUID.randomUUID(), vehicleId, date, items, cost,
                    nextDueDate, nextDueKm, Instant.now());
            return maintenanceRepo.save(record);
        });
    }

    /** 取得指定車輛的所有保養紀錄。 */
    public List<MaintenanceRecord> getRecords(UUID vehicleId) {
        return maintenanceRepo.findByVehicleId(vehicleId);
    }

    /**
     * 掃描所有保養紀錄，回傳已到期需提醒的紀錄清單。
     *
     * <p>到期判斷委派給 {@link MaintenanceRecord#isDue}（LoD）。</p>
     */
    public List<MaintenanceRecord> getDueReminders(LocalDate currentDate,
                                                    Map<UUID, Integer> currentKmMap) {
        return maintenanceRepo.findAll().stream()
                .filter(r -> r.isDue(currentDate,
                        currentKmMap.getOrDefault(r.getVehicleId(), 0)))
                .toList();
    }

    /**
     * 刪除保養紀錄。
     *
     * @throws PermissionDeniedException 若使用者不具備管理車輛的權限
     */
    public void deleteRecord(User actor, UUID recordId) {
        requirePermission(actor, Permission.MANAGE_VEHICLE,
                () -> maintenanceRepo.delete(recordId));
    }
}
