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
 * 車輛管理業務邏輯服務（Template Method Pattern）。
 *
 * <p><b>套用的設計原則：</b>
 * <ul>
 *   <li><b>SRP</b>：只負責車輛資產的 CRUD 與狀態管理，不處理借車流程。</li>
 *   <li><b>DIP</b>：依賴 {@link IVehicleRepository} 介面，不感知具體 DB 實作。</li>
 *   <li><b>Template Method（Ch7）</b>：繼承 {@link AbstractProtectedService}，
 *       權限驗證骨架由父類別定義，子類別只提供業務邏輯 lambda。</li>
 * </ul>
 */
@Service
public class VehicleService extends AbstractProtectedService {

    private final IVehicleRepository vehicleRepo;

    public VehicleService(IVehicleRepository vehicleRepo) {
        this.vehicleRepo = vehicleRepo;
    }

    /**
     * 新增車輛（初始狀態為 AVAILABLE）。
     *
     * @throws PermissionDeniedException 若使用者不具備 {@link Permission#MANAGE_VEHICLE}
     */
    public Vehicle createVehicle(User actor, String plate, String model, int year) {
        return supply(actor, Permission.MANAGE_VEHICLE, () -> {
            Vehicle vehicle = new Vehicle(UUID.randomUUID(), plate, model, year,
                    VehicleStatus.AVAILABLE, Instant.now());
            return vehicleRepo.save(vehicle);
        });
    }

    /**
     * 依 ID 取得車輛，若不存在則拋出例外。
     *
     * @throws ResourceNotFoundException 若車輛不存在
     */
    public Vehicle getVehicle(UUID id) {
        return vehicleRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + id));
    }

    /** 取得系統中所有車輛。 */
    public List<Vehicle> listAll() {
        return vehicleRepo.findAll();
    }

    /**
     * 查詢在指定時段內可借用的車輛。
     */
    public List<Vehicle> findAvailable(Instant start, Instant end) {
        return vehicleRepo.findAvailable(start, end);
    }

    /**
     * 更新車輛基本資料（車牌、車款、年份）。
     *
     * @throws PermissionDeniedException 若使用者不具備管理車輛的權限
     * @throws ResourceNotFoundException 若車輛 ID 不存在
     */
    public Vehicle updateVehicle(User actor, UUID id, String plate, String model, int year) {
        return supply(actor, Permission.MANAGE_VEHICLE, () -> {
            Vehicle v = getVehicle(id);
            Vehicle updated = new Vehicle(v.getId(), plate, model, year, v.getStatus(), v.getCreatedAt());
            return vehicleRepo.save(updated);
        });
    }

    /**
     * 刪除車輛。
     *
     * @throws PermissionDeniedException 若使用者不具備管理車輛的權限
     */
    public void deleteVehicle(User actor, UUID id) {
        requirePermission(actor, Permission.MANAGE_VEHICLE, () -> vehicleRepo.delete(id));
    }
}
