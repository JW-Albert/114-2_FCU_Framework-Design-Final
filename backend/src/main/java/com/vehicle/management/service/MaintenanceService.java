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
 * 保養紀錄管理業務邏輯服務。
 *
 * <p><b>套用的設計原則：</b>
 * <ul>
 *   <li><b>SRP</b>：只負責保養紀錄的新增、查詢、刪除，以及到期提醒掃描。</li>
 *   <li><b>DIP</b>：依賴 {@link IMaintenanceRepository} 介面，不感知具體 DB 實作。</li>
 * </ul>
 *
 * <p>到期判斷邏輯封裝在 {@link MaintenanceRecord#isDue}，
 * 遵守最少知識原則（LoD）——Service 只呼叫 {@code isDue()}，
 * 不直接讀取日期與里程欄位自行計算。</p>
 */
@Service
public class MaintenanceService {

    private final IMaintenanceRepository maintenanceRepo;

    /**
     * @param maintenanceRepo 保養紀錄儲存庫（由 Spring DI 注入）
     */
    public MaintenanceService(IMaintenanceRepository maintenanceRepo) {
        this.maintenanceRepo = maintenanceRepo;
    }

    /**
     * 新增保養紀錄。
     *
     * @param actor       執行操作的使用者
     * @param vehicleId   所屬車輛 ID
     * @param date        保養日期
     * @param items       保養項目清單
     * @param cost        保養費用
     * @param nextDueDate 下次建議保養日期（可為 {@code null}）
     * @param nextDueKm   下次建議保養里程（可為 {@code null}）
     * @return 已儲存的保養紀錄
     * @throws PermissionDeniedException 若使用者不具備 {@link Permission#MANAGE_VEHICLE}
     */
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

    /**
     * 取得指定車輛的所有保養紀錄。
     *
     * @param vehicleId 車輛唯一識別碼
     * @return 該車輛的保養紀錄清單
     */
    public List<MaintenanceRecord> getRecords(UUID vehicleId) {
        return maintenanceRepo.findByVehicleId(vehicleId);
    }

    /**
     * 掃描所有保養紀錄，回傳已到期需提醒的紀錄清單。
     *
     * <p>到期判斷委派給 {@link MaintenanceRecord#isDue}（LoD），
     * 同時考慮日期與里程兩種條件（任一達標即提醒）。</p>
     *
     * @param currentDate 查詢當下的日期
     * @param currentKmMap 各車輛目前里程的 Map（key: vehicleId, value: 里程數）
     * @return 到期需保養的紀錄清單
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
     * @param actor    執行操作的使用者
     * @param recordId 欲刪除的紀錄 ID
     * @throws PermissionDeniedException 若使用者不具備管理車輛的權限
     */
    public void deleteRecord(User actor, UUID recordId) {
        if (!actor.can(Permission.MANAGE_VEHICLE)) {
            throw new PermissionDeniedException(actor.getEmail() + " cannot manage vehicles");
        }
        maintenanceRepo.delete(recordId);
    }
}
