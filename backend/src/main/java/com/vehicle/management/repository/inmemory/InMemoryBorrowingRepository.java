package com.vehicle.management.repository.inmemory;

import com.vehicle.management.domain.model.BorrowingRequest;
import com.vehicle.management.repository.IBorrowingRepository;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryBorrowingRepository implements IBorrowingRepository {

    private final Map<UUID, BorrowingRequest> store = new ConcurrentHashMap<>();

    private static final Set<String> ACTIVE_STATES = Set.of("PENDING", "APPROVED", "IN_USE");

    @Override
    public Optional<BorrowingRequest> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<BorrowingRequest> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public List<BorrowingRequest> findPending() {
        return store.values().stream()
                .filter(r -> "PENDING".equals(r.getStateName()))
                .toList();
    }

    @Override
    public List<BorrowingRequest> findByUserId(UUID userId) {
        return store.values().stream()
                .filter(r -> userId.equals(r.getUserId()))
                .toList();
    }

    @Override
    public List<BorrowingRequest> findConflicting(UUID vehicleId, Instant start, Instant end) {
        return store.values().stream()
                .filter(r -> vehicleId.equals(r.getVehicleId()))
                .filter(r -> ACTIVE_STATES.contains(r.getStateName()))
                .filter(r -> start.isBefore(r.getPeriodEnd()) && end.isAfter(r.getPeriodStart()))
                .toList();
    }

    @Override
    public BorrowingRequest save(BorrowingRequest request) {
        store.put(request.getId(), request);
        return request;
    }
}
