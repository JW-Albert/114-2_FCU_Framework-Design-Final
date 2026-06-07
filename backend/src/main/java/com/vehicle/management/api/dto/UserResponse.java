package com.vehicle.management.api.dto;

import com.vehicle.management.domain.model.User;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        List<String> roles,
        Instant createdAt
) {
    public static UserResponse from(User u) {
        List<String> roleNames = u.getRoles().stream()
                .map(r -> r.getName())
                .sorted()
                .toList();
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), roleNames, u.getCreatedAt());
    }
}
