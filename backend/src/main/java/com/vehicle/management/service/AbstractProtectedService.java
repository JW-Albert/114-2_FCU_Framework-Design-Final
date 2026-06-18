package com.vehicle.management.service;

import com.vehicle.management.domain.model.User;
import com.vehicle.management.domain.role.Permission;

import java.util.function.Supplier;

/**
 * 帶有權限守衛的服務層基礎類別（Template Method Pattern）。
 *
 * <p>定義「先驗證權限，再執行業務邏輯」的演算法骨架（Template Method Pattern, GoF Ch14）。
 * 子類別繼承此類別並呼叫 {@link #requirePermission} 或 {@link #supply}，
 * 不需各自重複撰寫 if-throw 的授權判斷（消除 VehicleService / UserService /
 * MaintenanceService 之間的重複程式碼，符合 DRY 與 SRP）。</p>
 *
 * <p>若未來需要加入稽核日誌、速率限制等橫切關注點，只需在此基礎類別的
 * {@link #requirePermission} 方法加入即可，所有子服務自動受惠（OCP）。</p>
 */
public abstract class AbstractProtectedService {

    /**
     * 驗證使用者權限後執行無回傳值的業務邏輯（Template Method 骨架）。
     *
     * @param actor      執行操作的使用者
     * @param permission 所需權限
     * @param action     通過驗證後執行的業務邏輯
     * @throws PermissionDeniedException 若使用者不具備所需權限
     */
    protected void requirePermission(User actor, Permission permission, Runnable action) {
        checkPermission(actor, permission);
        action.run();
    }

    /**
     * 驗證使用者權限後執行有回傳值的業務邏輯（Template Method 骨架）。
     *
     * @param <T>        回傳值型別
     * @param actor      執行操作的使用者
     * @param permission 所需權限
     * @param action     通過驗證後執行的業務邏輯
     * @return 業務邏輯的執行結果
     * @throws PermissionDeniedException 若使用者不具備所需權限
     */
    protected <T> T supply(User actor, Permission permission, Supplier<T> action) {
        checkPermission(actor, permission);
        return action.get();
    }

    private void checkPermission(User actor, Permission permission) {
        if (!actor.can(permission)) {
            throw new PermissionDeniedException(
                    actor.getEmail() + " lacks permission: " + permission.name());
        }
    }
}
