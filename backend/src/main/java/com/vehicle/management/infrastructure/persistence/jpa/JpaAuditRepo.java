package com.vehicle.management.infrastructure.persistence.jpa;

import com.vehicle.management.infrastructure.persistence.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaAuditRepo extends JpaRepository<AuditLogEntity, UUID> {
    List<AuditLogEntity> findByOrderByCreatedAtDesc();
}
