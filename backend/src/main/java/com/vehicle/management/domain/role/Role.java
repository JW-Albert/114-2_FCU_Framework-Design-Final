package com.vehicle.management.domain.role;

import java.util.Set;

/**
 * 使用者角色介面（Role Interface）。
 *
 * <p><b>OOP 設計重點：</b>
 * <ul>
 *   <li><b>介面多型（Interface Polymorphism）</b>：{@link com.vehicle.management.domain.model.User}
 *       以 {@code Set<Role>} 持有角色，透過多型呼叫 {@link #getPermissions()}，
 *       無需感知 {@link AdminRole} 或 {@link EmployeeRole} 等具體類別。</li>
 *   <li><b>依賴反轉原則（DIP）</b>：{@link com.vehicle.management.domain.role.RoleFactory}
 *       回傳此介面，呼叫端只依賴 {@code Role}，不依賴具體實作。</li>
 *   <li><b>開放封閉原則（OCP）</b>：新增角色只需實作此介面並更新 {@link RoleFactory}，
 *       不需修改任何現有類別。</li>
 * </ul>
 */
public interface Role {

    /**
     * 取得角色名稱。
     *
     * @return 大寫角色字串，例如 {@code "ADMIN"} 或 {@code "EMPLOYEE"}
     */
    String getName();

    /**
     * 取得此角色擁有的所有操作權限集合。
     *
     * @return 不可變的 {@link Permission} 集合
     */
    Set<Permission> getPermissions();
}
