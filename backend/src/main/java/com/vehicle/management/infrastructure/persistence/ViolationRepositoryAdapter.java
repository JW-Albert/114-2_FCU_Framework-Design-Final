package com.vehicle.management.infrastructure.persistence;

import com.vehicle.management.domain.model.ViolationRecord;
import com.vehicle.management.infrastructure.persistence.entity.ViolationRecordEntity;
import com.vehicle.management.infrastructure.persistence.jpa.JpaViolationRepo;
import com.vehicle.management.repository.IViolationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * 違規記錄儲存庫 Adapter（Adapter Pattern，Ch14）。
 *
 * <p>將 JPA 實作（{@link JpaViolationRepo}）適配到業務層介面（{@link IViolationRepository}），
 * 並負責領域物件與 JPA Entity 之間的雙向轉換。</p>
 */
@Repository
public class ViolationRepositoryAdapter implements IViolationRepository {

    private final JpaViolationRepo jpa;

    public ViolationRepositoryAdapter(JpaViolationRepo jpa) {
        this.jpa = jpa;
    }

    @Override
    public ViolationRecord save(ViolationRecord v) {
        return toDomain(jpa.save(toEntity(v)));
    }

    @Override
    public List<ViolationRecord> findAll() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<ViolationRecord> findByUserId(UUID userId) {
        return jpa.findByUserId(userId).stream().map(this::toDomain).toList();
    }

    private ViolationRecord toDomain(ViolationRecordEntity e) {
        return new ViolationRecord(
                e.getId(), e.getUserId(), e.getVehicleId(), e.getBorrowingId(),
                e.getType(), e.getDescription(), e.getCreatedAt());
    }

    private ViolationRecordEntity toEntity(ViolationRecord v) {
        ViolationRecordEntity e = new ViolationRecordEntity();
        e.setId(v.getId());
        e.setUserId(v.getUserId());
        e.setVehicleId(v.getVehicleId());
        e.setBorrowingId(v.getBorrowingId());
        e.setType(v.getType());
        e.setDescription(v.getDescription());
        e.setCreatedAt(v.getCreatedAt());
        return e;
    }
}
