package com.vehicle.management.domain.role;

import java.util.Set;

/**
 * 部門主管角色實作（ConcreteRole）。
 *
 * <p>部門主管擁有審核本部門借車申請及送出申請的權限：
 * <ul>
 *   <li>{@link Permission#APPROVE_BORROWING} — 審核（限同部門申請）</li>
 *   <li>{@link Permission#SUBMIT_REQUEST}    — 送出借車申請</li>
 * </ul>
 *
 * <p>實作 {@link Role} 介面，由 {@link RoleFactory} 統一建立（Factory Method Pattern）。
 * 部門範圍限制由 {@link com.vehicle.management.service.BorrowingService} 負責執行。</p>
 */
public class ManagerRole implements Role {

    /**
     * 部門主管擁有的權限集合（靜態不可變）。
     */
    private static final Set<Permission> PERMISSIONS = Set.of(
            Permission.APPROVE_BORROWING,
            Permission.SUBMIT_REQUEST
    );

    /**
     * {@inheritDoc}
     *
     * @return {@code "MANAGER"}
     */
    @Override
    public String getName() {
        return "MANAGER";
    }

    /**
     * {@inheritDoc}
     *
     * @return 包含審核與送出申請兩種權限的不可變集合
     */
    @Override
    public Set<Permission> getPermissions() {
        return PERMISSIONS;
    }
}
