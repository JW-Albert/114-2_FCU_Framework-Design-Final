package com.vehicle.management.domain.role;

/**
 * Factory Method (Ch11): 將 Role 物件的建立集中於此，
 * 呼叫端只依賴 Role 介面，不感知具體實作類別（OCP + DIP）。
 */
public class RoleFactory {

    public static Role create(String roleName) {
        return switch (roleName.toUpperCase()) {
            case "ADMIN"    -> new AdminRole();
            case "EMPLOYEE" -> new EmployeeRole();
            default -> throw new IllegalArgumentException("Unknown role: " + roleName);
        };
    }
}
