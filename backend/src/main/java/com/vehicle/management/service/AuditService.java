package com.vehicle.management.service;

import com.vehicle.management.domain.model.AuditLog;
import com.vehicle.management.domain.model.User;
import com.vehicle.management.domain.role.Permission;
import com.vehicle.management.repository.IAuditRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 稽核日誌服務。
 *
 * <p>{@link BorrowingCommandBus} 於派送命令時呼叫 {@link #record} 寫入稽核紀錄；
 * 管理員可透過 {@link #listRecent} 查詢（需 {@link Permission#MANAGE_USER}）。</p>
 */
@Service
public class AuditService {

    private final IAuditRepository repo;

    public AuditService(IAuditRepository repo) {
        this.repo = repo;
    }

    /**
     * 記錄一筆稽核日誌。
     *
     * @param action   操作類型
     * @param detail   操作明細
     * @param targetId 受影響的目標 ID（可為 null）
     */
    public void record(String action, String detail, UUID targetId) {
        repo.save(new AuditLog(UUID.randomUUID(), action, detail, targetId, Instant.now()));
    }

    /**
     * 查詢所有稽核日誌（新到舊），限管理員。
     *
     * @param caller 執行查詢者，須具備 {@link Permission#MANAGE_USER}
     * @throws PermissionDeniedException 若呼叫者非管理員
     */
    public List<AuditLog> listRecent(User caller) {
        if (!caller.can(Permission.MANAGE_USER)) {
            throw new PermissionDeniedException("Only admins can view audit logs");
        }
        return repo.findAllRecent();
    }
}
