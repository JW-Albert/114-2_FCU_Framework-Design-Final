package com.vehicle.management.domain.role;

import java.util.Set;

/**
 * 管理員角色實作（ConcreteRole）。
 *
 * <p>管理員擁有系統全部操作權限：
 * <ul>
 *   <li>{@link Permission#APPROVE_BORROWING} — 審核、出車、還車</li>
 *   <li>{@link Permission#MANAGE_VEHICLE}    — 車輛與保養資料管理</li>
 *   <li>{@link Permission#MANAGE_USER}       — 使用者帳號管理</li>
 *   <li>{@link Permission#MANAGE_VIOLATION}  — 手動登錄違規記錄</li>
 *   <li>{@link Permission#SUBMIT_REQUEST}    — 送出借車申請</li>
 * </ul>
 *
 * <p>實作 {@link Role} 介面，由 {@link RoleFactory} 統一建立，
 * 呼叫端（Service 層）不直接依賴此類別（DIP）。</p>
 */
public class AdminRole implements Role {

    /**
     * 管理員擁有的完整權限集合（靜態不可變）。
     * 使用 {@link Set#of} 確保集合不可修改。
     */
    private static final Set<Permission> PERMISSIONS = Set.of(
            Permission.APPROVE_BORROWING,
            Permission.MANAGE_VEHICLE,
            Permission.MANAGE_USER,
            Permission.MANAGE_VIOLATION,
            Permission.SUBMIT_REQUEST
    );

    /**
     * {@inheritDoc}
     *
     * @return {@code "ADMIN"}
     */
    @Override
    public String getName() {
        return "ADMIN";
    }

    /**
     * {@inheritDoc}
     *
     * @return 包含四種系統權限的不可變集合
     */
    @Override
    public Set<Permission> getPermissions() {
        return PERMISSIONS;
    }
}
