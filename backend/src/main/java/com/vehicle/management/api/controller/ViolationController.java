package com.vehicle.management.api.controller;

import com.vehicle.management.api.dto.CreateViolationRequest;
import com.vehicle.management.api.dto.ViolationResponse;
import com.vehicle.management.domain.model.User;
import com.vehicle.management.service.UserService;
import com.vehicle.management.service.ViolationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 違規記錄 REST 控制器。
 *
 * <p>提供：
 * <ul>
 *   <li>GET  /api/violations — 取得所有違規記錄（管理員用）</li>
 *   <li>GET  /api/violations/user/{userId} — 取得指定使用者的違規記錄</li>
 *   <li>POST /api/violations — 手動登錄違規記錄（管理員 / 主管，需 MANAGE_VIOLATION 權限）</li>
 * </ul>
 * 所有端點均需要身分驗證（由 SecurityConfig 設定），細粒度權限由 Service 層把關。</p>
 */
@RestController
@RequestMapping("/api/violations")
public class ViolationController {

    private final ViolationService violationService;
    private final UserService userService;

    public ViolationController(ViolationService violationService, UserService userService) {
        this.violationService = violationService;
        this.userService = userService;
    }

    /**
     * 手動登錄違規記錄。
     *
     * @param principal 目前登入者（由 JWT 過濾器注入）
     * @param req       違規登錄請求（借用記錄 ID、類型、描述）
     * @return 已建立的違規記錄
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ViolationResponse create(@AuthenticationPrincipal UserDetails principal,
                                    @Valid @RequestBody CreateViolationRequest req) {
        User actor = userService.findByEmail(principal.getUsername());
        return ViolationResponse.from(violationService.createManual(
                actor, req.borrowingId(), req.type(), req.description()));
    }

    /**
     * 取得所有違規記錄。
     *
     * @return 所有違規記錄清單
     */
    @GetMapping
    public List<ViolationResponse> listAll() {
        return violationService.listAll().stream()
                .map(ViolationResponse::from)
                .toList();
    }

    /**
     * 取得指定使用者的違規記錄。
     *
     * @param userId 使用者唯一識別碼
     * @return 該使用者的違規記錄清單
     */
    @GetMapping("/user/{userId}")
    public List<ViolationResponse> listByUser(@PathVariable UUID userId) {
        return violationService.listByUser(userId).stream()
                .map(ViolationResponse::from)
                .toList();
    }
}
