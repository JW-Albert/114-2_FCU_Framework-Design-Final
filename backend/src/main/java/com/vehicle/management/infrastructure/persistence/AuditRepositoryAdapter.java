package com.vehicle.management.infrastructure.persistence;

import com.vehicle.management.domain.model.AuditLog;
import com.vehicle.management.infrastructure.persistence.entity.AuditLogEntity;
import com.vehicle.management.infrastructure.persistence.jpa.JpaAuditRepo;
import com.vehicle.management.repository.IAuditRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Adapter（Ch14）：JPA AuditLogEntity ↔ Domain AuditLog。 */
@Repository
public class AuditRepositoryAdapter implements IAuditRepository {

    private final JpaAuditRepo jpa;

    public AuditRepositoryAdapter(JpaAuditRepo jpa) {
        this.jpa = jpa;
    }

    @Override
    public AuditLog save(AuditLog log) {
        return toDomain(jpa.save(toEntity(log)));
    }

    @Override
    public List<AuditLog> findAllRecent() {
        return jpa.findByOrderByCreatedAtDesc().stream().map(this::toDomain).toList();
    }

    private AuditLog toDomain(AuditLogEntity e) {
        return new AuditLog(e.getId(), e.getAction(), e.getDetail(), e.getTargetId(), e.getCreatedAt());
    }

    private AuditLogEntity toEntity(AuditLog a) {
        AuditLogEntity e = new AuditLogEntity();
        e.setId(a.getId());
        e.setAction(a.getAction());
        e.setDetail(a.getDetail());
        e.setTargetId(a.getTargetId());
        e.setCreatedAt(a.getCreatedAt());
        return e;
    }
}
