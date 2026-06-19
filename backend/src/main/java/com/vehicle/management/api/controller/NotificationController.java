package com.vehicle.management.api.controller;

import com.vehicle.management.api.dto.NotificationResponse;
import com.vehicle.management.domain.model.User;
import com.vehicle.management.service.NotificationService;
import com.vehicle.management.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 站內通知（收件夾）REST 控制器。
 *
 * <p>所有端點皆以目前登入者為作用對象——使用者只能存取自己的收件夾。</p>
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    /** 取得目前使用者的收件夾（新到舊）。 */
    @GetMapping
    public List<NotificationResponse> list(@AuthenticationPrincipal UserDetails principal) {
        User actor = userService.findByEmail(principal.getUsername());
        return notificationService.listForUser(actor.getId()).stream()
                .map(NotificationResponse::from).toList();
    }

    /** 取得未讀數量（導覽列狀態燈用）。 */
    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(@AuthenticationPrincipal UserDetails principal) {
        User actor = userService.findByEmail(principal.getUsername());
        return Map.of("count", notificationService.countUnread(actor.getId()));
    }

    /** 將指定通知標記為已讀。 */
    @PostMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@AuthenticationPrincipal UserDetails principal, @PathVariable UUID id) {
        User actor = userService.findByEmail(principal.getUsername());
        notificationService.markAsRead(actor.getId(), id);
    }

    /** 將目前使用者所有未讀通知標記為已讀。 */
    @PostMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllRead(@AuthenticationPrincipal UserDetails principal) {
        User actor = userService.findByEmail(principal.getUsername());
        notificationService.markAllAsRead(actor.getId());
    }
}
