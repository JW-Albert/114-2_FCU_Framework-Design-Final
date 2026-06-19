package com.vehicle.management.api.controller;

import com.vehicle.management.api.dto.AuthResponse;
import com.vehicle.management.api.dto.LoginRequest;
import com.vehicle.management.api.dto.RegisterRequest;
import com.vehicle.management.domain.model.User;
import com.vehicle.management.infrastructure.security.JwtUtil;
import com.vehicle.management.service.LoginAttemptService;
import com.vehicle.management.service.ResourceNotFoundException;
import com.vehicle.management.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final LoginAttemptService loginAttemptService;

    public AuthController(UserService userService, JwtUtil jwtUtil,
                          LoginAttemptService loginAttemptService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.loginAttemptService = loginAttemptService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        User user = userService.register(req.name(), req.email(), req.password(), req.role());
        String token = jwtUtil.generate(user.getEmail());
        List<String> roles = user.getRoles().stream().map(r -> r.getName().toLowerCase()).toList();
        return new AuthResponse(token, user.getEmail(), user.getName(), roles, user.getDepartment());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        // 登入失敗鎖定：先檢查是否在鎖定期間
        loginAttemptService.assertNotLocked(req.email());

        User user;
        try {
            user = userService.findByEmail(req.email());
        } catch (ResourceNotFoundException e) {
            // 帳號不存在也記錄失敗，並回傳一致的錯誤（避免帳號枚舉）
            loginAttemptService.recordFailure(req.email());
            throw new BadCredentialsException("Invalid credentials");
        }
        if (!userService.verifyPassword(user, req.password())) {
            loginAttemptService.recordFailure(req.email());
            throw new BadCredentialsException("Invalid password");
        }

        loginAttemptService.recordSuccess(req.email());
        String token = jwtUtil.generate(user.getEmail());
        List<String> roles = user.getRoles().stream().map(r -> r.getName().toLowerCase()).toList();
        return new AuthResponse(token, user.getEmail(), user.getName(), roles, user.getDepartment());
    }
}
