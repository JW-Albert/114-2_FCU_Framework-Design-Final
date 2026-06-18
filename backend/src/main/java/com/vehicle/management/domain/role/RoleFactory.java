package com.vehicle.management.domain.role;

/**
 * 角色工廠（Factory Method Pattern，Ch11）。
 *
 * <p>將 {@link Role} 物件的建立邏輯集中於此，
 * 呼叫端（{@link com.vehicle.management.service.UserService} 等）
 * 只依賴 {@link Role} 介面，不直接 {@code new AdminRole()} 或 {@code new EmployeeRole()}，
 * 符合依賴反轉原則（DIP）與開放封閉原則（OCP）。</p>
 *
 * <p><b>擴充方式：</b>新增角色只需：
 * <ol>
 *   <li>建立新的 {@code XxxRole implements Role} 類別；</li>
 *   <li>在 {@link #create(String)} 的 {@code switch} 加入對應 case。</li>
 * </ol>
 * 其他所有類別無需改動。</p>
 */
public class RoleFactory {

    /** 防止工具類別被實例化。 */
    private RoleFactory() {}

    /**
     * 依角色名稱字串建立對應的 {@link Role} 實作。
     *
     * @param roleName 角色名稱（大小寫不敏感），目前支援 {@code "ADMIN"} 與 {@code "EMPLOYEE"}
     * @return 對應的 {@link Role} 實作物件
     * @throws IllegalArgumentException 若 {@code roleName} 不對應任何已知角色
     */
    public static Role create(String roleName) {
        return switch (roleName.toUpperCase()) {
            case "ADMIN"    -> new AdminRole();
            case "EMPLOYEE" -> new EmployeeRole();
            case "MANAGER"  -> new ManagerRole();
            default -> throw new IllegalArgumentException("Unknown role: " + roleName);
        };
    }
}
