package com.vehicle.management.api.controller;

import com.vehicle.management.api.dto.AuditResponse;
import com.vehicle.management.domain.model.User;
import com.vehicle.management.service.AuditService;
import com.vehicle.management.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 稽核日誌查詢控制器（管理員）。
 */
@RestController
@RequestMapping("/api/audit-logs")
public class AuditController {

    private final AuditService auditService;
    private final UserService userService;

    public AuditController(AuditService auditService, UserService userService) {
        this.auditService = auditService;
        this.userService = userService;
    }

    /** 查詢所有稽核日誌（新到舊），限管理員。 */
    @GetMapping
    public List<AuditResponse> list(@AuthenticationPrincipal UserDetails principal) {
        User actor = userService.findByEmail(principal.getUsername());
        return auditService.listRecent(actor).stream().map(AuditResponse::from).toList();
    }
}
