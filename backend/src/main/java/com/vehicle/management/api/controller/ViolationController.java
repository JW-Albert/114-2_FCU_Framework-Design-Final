package com.vehicle.management.api.controller;

import com.vehicle.management.api.dto.ViolationResponse;
import com.vehicle.management.service.ViolationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 違規記錄 REST 控制器。
 *
 * <p>提供：
 * <ul>
 *   <li>GET /api/violations — 取得所有違規記錄（管理員用）</li>
 *   <li>GET /api/violations/user/{userId} — 取得指定使用者的違規記錄</li>
 * </ul>
 * 所有端點均需要身分驗證（由 SecurityConfig 設定）。</p>
 */
@RestController
@RequestMapping("/api/violations")
public class ViolationController {

    private final ViolationService violationService;

    public ViolationController(ViolationService violationService) {
        this.violationService = violationService;
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
