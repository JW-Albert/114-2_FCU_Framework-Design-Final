package com.vehicle.management.domain.role;

import java.util.Set;

public class EmployeeRole implements Role {

    private static final Set<Permission> PERMISSIONS = Set.of(
            Permission.SUBMIT_REQUEST
    );

    @Override
    public String getName() {
        return "EMPLOYEE";
    }

    @Override
    public Set<Permission> getPermissions() {
        return PERMISSIONS;
    }
}
