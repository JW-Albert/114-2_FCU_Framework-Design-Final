package com.vehicle.management.infrastructure.persistence;

import com.vehicle.management.domain.model.BorrowingRequest;
import com.vehicle.management.infrastructure.persistence.entity.BorrowingRequestEntity;
import com.vehicle.management.infrastructure.persistence.jpa.JpaBorrowingRepo;
import com.vehicle.management.repository.IBorrowingRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Adapter (Ch14): 將 JPA BorrowingRequestEntity 轉換為 Domain BorrowingRequest。 */
@Repository
public class BorrowingRepositoryAdapter implements IBorrowingRepository {

    private final JpaBorrowingRepo jpa;

    public BorrowingRepositoryAdapter(JpaBorrowingRepo jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<BorrowingRequest> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<BorrowingRequest> findAll() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<BorrowingRequest> findPending() {
        return jpa.findByState("PENDING").stream().map(this::toDomain).toList();
    }

    @Override
    public List<BorrowingRequest> findByUserId(UUID userId) {
        return jpa.findByUserId(userId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<BorrowingRequest> findConflicting(UUID vehicleId, Instant start, Instant end) {
        return jpa.findConflicting(vehicleId, start, end).stream().map(this::toDomain).toList();
    }

    @Override
    public BorrowingRequest save(BorrowingRequest request) {
        return toDomain(jpa.save(toEntity(request)));
    }

    private BorrowingRequest toDomain(BorrowingRequestEntity e) {
        BorrowingRequest r = new BorrowingRequest(
                e.getId(), e.getUserId(), e.getVehicleId(),
                e.getPeriodStart(), e.getPeriodEnd(), e.getPurpose(), e.getCreatedAt());
        // Restore state by replaying transitions
        int endMil = e.getEndMileage() != null ? e.getEndMileage() : 0;
        switch (e.getState()) {
            case "APPROVED" -> r.approve(e.getReviewNote());
            case "REJECTED" -> r.reject(e.getReviewNote());
            case "IN_USE"   -> { r.approve(null); r.startUse(); }
            case "RETURNED" -> { r.approve(null); r.startUse(); r.complete(endMil); }
            // PENDING: no replay needed
        }
        r.restoreMileage(e.getStartMileage(), e.getEndMileage());
        return r;
    }

    private BorrowingRequestEntity toEntity(BorrowingRequest r) {
        BorrowingRequestEntity e = new BorrowingRequestEntity();
        e.setId(r.getId());
        e.setUserId(r.getUserId());
        e.setVehicleId(r.getVehicleId());
        e.setPeriodStart(r.getPeriodStart());
        e.setPeriodEnd(r.getPeriodEnd());
        e.setPurpose(r.getPurpose());
        e.setState(r.getStateName());
        e.setReviewNote(r.getReviewNote());
        e.setCreatedAt(r.getCreatedAt());
        e.setUpdatedAt(r.getUpdatedAt());
        e.setStartMileage(r.getStartMileage());
        e.setEndMileage(r.getEndMileage());
        return e;
    }
}
