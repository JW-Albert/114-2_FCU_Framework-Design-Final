package com.vehicle.management.api.dto;

import java.util.List;

public record AuthResponse(String token, String email, String name, List<String> roles, String department) {}
