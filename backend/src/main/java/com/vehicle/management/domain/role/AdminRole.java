package com.vehicle.management.domain.role;

import java.util.Set;

public class AdminRole implements Role {

    private static final Set<Permission> PERMISSIONS = Set.of(
            Permission.APPROVE_BORROWING,
            Permission.MANAGE_VEHICLE,
            Permission.MANAGE_USER,
            Permission.SUBMIT_REQUEST
    );

    @Override
    public String getName() {
        return "ADMIN";
    }

    @Override
    public Set<Permission> getPermissions() {
        return PERMISSIONS;
    }
}
