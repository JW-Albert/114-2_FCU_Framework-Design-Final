package com.vehicle.management.domain.role;

import java.util.Set;

public interface Role {
    String getName();
    Set<Permission> getPermissions();
}
