package com.vehicle.management.infrastructure.persistence.jpa;

import com.vehicle.management.infrastructure.persistence.entity.ViolationRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JpaViolationRepo extends JpaRepository<ViolationRecordEntity, UUID> {
    List<ViolationRecordEntity> findByUserId(UUID userId);
    List<ViolationRecordEntity> findByBorrowingId(UUID borrowingId);
}
