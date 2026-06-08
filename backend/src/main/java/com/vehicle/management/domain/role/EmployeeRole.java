package com.vehicle.management.domain.role;

import java.util.Set;

/**
 * 員工角色實作（ConcreteRole）。
 *
 * <p>員工只擁有送出借車申請的權限：
 * <ul>
 *   <li>{@link Permission#SUBMIT_REQUEST} — 送出借車申請</li>
 * </ul>
 *
 * <p>實作 {@link Role} 介面，由 {@link RoleFactory} 統一建立（Factory Method Pattern）。
 * 若需擴充員工權限，只需修改此類別的 {@code PERMISSIONS} 集合，無需改動其他類別（OCP）。</p>
 */
public class EmployeeRole implements Role {

    /**
     * 員工擁有的權限集合（靜態不可變）。
     */
    private static final Set<Permission> PERMISSIONS = Set.of(
            Permission.SUBMIT_REQUEST
    );

    /**
     * {@inheritDoc}
     *
     * @return {@code "EMPLOYEE"}
     */
    @Override
    public String getName() {
        return "EMPLOYEE";
    }

    /**
     * {@inheritDoc}
     *
     * @return 僅含 {@link Permission#SUBMIT_REQUEST} 的不可變集合
     */
    @Override
    public Set<Permission> getPermissions() {
        return PERMISSIONS;
    }
}
