package com.vehicle.management.repository;

import com.vehicle.management.domain.model.BorrowingRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IBorrowingRepository {
    Optional<BorrowingRequest> findById(UUID id);
    List<BorrowingRequest> findAll();
    List<BorrowingRequest> findPending();
    List<BorrowingRequest> findByUserId(UUID userId);
    List<BorrowingRequest> findConflicting(UUID vehicleId, Instant start, Instant end);
    BorrowingRequest save(BorrowingRequest request);
}
