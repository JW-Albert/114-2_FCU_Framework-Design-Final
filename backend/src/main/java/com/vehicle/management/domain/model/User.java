package com.vehicle.management.domain.model;

import com.vehicle.management.domain.role.Permission;
import com.vehicle.management.domain.role.Role;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class User {

    private final UUID id;
    private final String name;
    private final String email;
    private final String passwordHash;
    private final Set<Role> roles;
    private final Instant createdAt;

    public User(UUID id, String name, String email, String passwordHash,
                Set<Role> roles, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roles = new HashSet<>(roles);
        this.createdAt = createdAt;
    }

    /**
     * Checks whether this user holds the given permission through any of their roles.
     * Service layer must only call this method — never inspect roles directly.
     */
    public boolean can(Permission permission) {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(p -> p == permission);
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Set<Role> getRoles() { return Set.copyOf(roles); }
    public Instant getCreatedAt() { return createdAt; }
}
