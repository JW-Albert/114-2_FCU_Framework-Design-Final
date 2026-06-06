package com.vehicle.management.api.controller;

import com.vehicle.management.api.dto.AuthResponse;
import com.vehicle.management.api.dto.LoginRequest;
import com.vehicle.management.api.dto.RegisterRequest;
import com.vehicle.management.domain.model.User;
import com.vehicle.management.infrastructure.security.JwtUtil;
import com.vehicle.management.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        User user = userService.register(req.name(), req.email(), req.password(), req.role());
        String token = jwtUtil.generate(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getName());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        User user = userService.findByEmail(req.email());
        if (!userService.verifyPassword(user, req.password())) {
            throw new org.springframework.security.authentication.BadCredentialsException("Invalid password");
        }
        String token = jwtUtil.generate(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getName());
    }
}
