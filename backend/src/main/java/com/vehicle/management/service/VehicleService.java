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
 * 車輛管理業務邏輯服務。
 *
 * <p><b>套用的設計原則：</b>
 * <ul>
 *   <li><b>SRP</b>：只負責車輛資產的 CRUD 與狀態管理，不處理借車流程。</li>
 *   <li><b>DIP</b>：依賴 {@link IVehicleRepository} 介面，不感知具體 DB 實作。</li>
 * </ul>
 *
 * <p>更新車輛資料時採用「不可變物件替換」策略：
 * 建立包含新欄位值的新 {@link Vehicle} 物件再儲存，
 * 而非直接修改現有物件，保持領域物件的不可變性。</p>
 */
@Service
public class VehicleService {

    private final IVehicleRepository vehicleRepo;

    /**
     * @param vehicleRepo 車輛儲存庫（由 Spring DI 注入）
     */
    public VehicleService(IVehicleRepository vehicleRepo) {
        this.vehicleRepo = vehicleRepo;
    }

    /**
     * 新增車輛（初始狀態為 AVAILABLE）。
     *
     * @param actor  執行操作的使用者
     * @param plate  車牌號碼
     * @param model  車款名稱
     * @param year   出廠年份
     * @return 已儲存的新車輛
     * @throws PermissionDeniedException 若使用者不具備 {@link Permission#MANAGE_VEHICLE}
     */
    public Vehicle createVehicle(User actor, String plate, String model, int year) {
        checkPermission(actor, Permission.MANAGE_VEHICLE);
        Vehicle vehicle = new Vehicle(UUID.randomUUID(), plate, model, year,
                VehicleStatus.AVAILABLE, Instant.now());
        return vehicleRepo.save(vehicle);
    }

    /**
     * 依 ID 取得車輛，若不存在則拋出例外。
     *
     * @param id 車輛唯一識別碼
     * @return 車輛物件
     * @throws ResourceNotFoundException 若車輛不存在
     */
    public Vehicle getVehicle(UUID id) {
        return vehicleRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + id));
    }

    /**
     * 取得系統中所有車輛。
     *
     * @return 全部車輛清單
     */
    public List<Vehicle> listAll() {
        return vehicleRepo.findAll();
    }

    /**
     * 查詢在指定時段內可借用的車輛。
     *
     * @param start 借用開始時間
     * @param end   借用結束時間
     * @return 無衝突的可用車輛清單
     */
    public List<Vehicle> findAvailable(Instant start, Instant end) {
        return vehicleRepo.findAvailable(start, end);
    }

    /**
     * 更新車輛基本資料（車牌、車款、年份）。
     * 採用不可變物件替換策略，保留原有 ID、狀態與建立時間。
     *
     * @param actor 執行操作的使用者
     * @param id    欲更新的車輛 ID
     * @param plate 新車牌號碼
     * @param model 新車款名稱
     * @param year  新出廠年份
     * @return 已更新的車輛物件
     * @throws PermissionDeniedException 若使用者不具備管理車輛的權限
     * @throws ResourceNotFoundException 若車輛 ID 不存在
     */
    public Vehicle updateVehicle(User actor, UUID id, String plate, String model, int year) {
        checkPermission(actor, Permission.MANAGE_VEHICLE);
        Vehicle v = getVehicle(id);
        // 不可變替換：建立新物件，保留原有狀態與建立時間
        Vehicle updated = new Vehicle(v.getId(), plate, model, year, v.getStatus(), v.getCreatedAt());
        return vehicleRepo.save(updated);
    }

    /**
     * 刪除車輛。
     *
     * @param actor 執行操作的使用者
     * @param id    欲刪除的車輛 ID
     * @throws PermissionDeniedException 若使用者不具備管理車輛的權限
     */
    public void deleteVehicle(User actor, UUID id) {
        checkPermission(actor, Permission.MANAGE_VEHICLE);
        vehicleRepo.delete(id);
    }

    /**
     * 驗證使用者是否擁有指定權限，不符則拋出例外。
     *
     * @param actor      執行操作的使用者
     * @param permission 所需的操作權限
     * @throws PermissionDeniedException 若使用者不具備該權限
     */
    private void checkPermission(User actor, Permission permission) {
        if (!actor.can(permission)) {
            throw new PermissionDeniedException(
                    actor.getEmail() + " lacks permission: " + permission);
        }
    }
}
