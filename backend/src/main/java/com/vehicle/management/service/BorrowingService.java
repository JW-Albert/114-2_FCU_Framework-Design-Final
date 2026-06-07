package com.vehicle.management.service;

import com.vehicle.management.domain.model.BorrowingRequest;
import com.vehicle.management.domain.model.User;
import com.vehicle.management.domain.observer.BorrowingEventPublisher;
import com.vehicle.management.domain.role.Permission;
import com.vehicle.management.domain.strategy.ConflictCheckStrategy;
import com.vehicle.management.domain.strategy.StrictOverlapStrategy;
import com.vehicle.management.repository.IBorrowingRepository;
import com.vehicle.management.repository.IVehicleRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * SRP: 只負責借車申請工作流程。
 * Observer（Ch20）: 繼承 BorrowingEventPublisher，狀態改變時廣播通知。
 * Strategy（Ch18）: 注入 ConflictCheckStrategy，衝突檢查演算法可替換。
 * 迪米特法則: 呼叫 request.approve() 委派給 State Pattern，不直接操作狀態字串。
 */
@Service
public class BorrowingService extends BorrowingEventPublisher {

    private final IBorrowingRepository borrowingRepo;
    private final IVehicleRepository vehicleRepo;
    private final ConflictCheckStrategy conflictStrategy;

    public BorrowingService(IBorrowingRepository borrowingRepo,
                            IVehicleRepository vehicleRepo) {
        this.borrowingRepo = borrowingRepo;
        this.vehicleRepo = vehicleRepo;
        this.conflictStrategy = new StrictOverlapStrategy();
    }

    public BorrowingRequest submitRequest(User actor, UUID vehicleId,
                                          Instant start, Instant end, String purpose) {
        if (!actor.can(Permission.SUBMIT_REQUEST)) {
            throw new PermissionDeniedException(actor.getEmail() + " cannot submit requests");
        }
        vehicleRepo.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + vehicleId));

        List<BorrowingRequest> conflicts = borrowingRepo.findConflicting(vehicleId, start, end);
        if (conflictStrategy.hasConflict(conflicts, start, end)) {
            throw new ConflictException("Vehicle is already booked for this period");
        }

        BorrowingRequest request = new BorrowingRequest(
                UUID.randomUUID(), actor.getId(), vehicleId, start, end, purpose, Instant.now());
        return borrowingRepo.save(request);
    }

    public BorrowingRequest approveRequest(User actor, UUID requestId, String note) {
        if (!actor.can(Permission.APPROVE_BORROWING)) {
            throw new PermissionDeniedException(actor.getEmail() + " cannot approve requests");
        }
        BorrowingRequest request = getRequest(requestId);
        request.approve(note);
        BorrowingRequest saved = borrowingRepo.save(request);
        notifyApproved(saved);
        return saved;
    }

    public BorrowingRequest rejectRequest(User actor, UUID requestId, String note) {
        if (!actor.can(Permission.APPROVE_BORROWING)) {
            throw new PermissionDeniedException(actor.getEmail() + " cannot reject requests");
        }
        BorrowingRequest request = getRequest(requestId);
        request.reject(note);
        BorrowingRequest saved = borrowingRepo.save(request);
        notifyRejected(saved);
        return saved;
    }

    public BorrowingRequest startUse(UUID requestId) {
        BorrowingRequest request = getRequest(requestId);
        vehicleRepo.findById(request.getVehicleId()).ifPresent(v -> {
            v.markInUse();
            vehicleRepo.save(v);
        });
        request.startUse();
        BorrowingRequest saved = borrowingRepo.save(request);
        notifyStarted(saved);
        return saved;
    }

    public BorrowingRequest completeUse(UUID requestId) {
        BorrowingRequest request = getRequest(requestId);
        vehicleRepo.findById(request.getVehicleId()).ifPresent(v -> {
            v.markAvailable();
            vehicleRepo.save(v);
        });
        request.complete();
        BorrowingRequest saved = borrowingRepo.save(request);
        notifyCompleted(saved);
        return saved;
    }

    public List<BorrowingRequest> listPending() {
        return borrowingRepo.findPending();
    }

    public List<BorrowingRequest> listMyRequests(UUID userId) {
        return borrowingRepo.findByUserId(userId);
    }

    public List<BorrowingRequest> listAll() {
        return borrowingRepo.findAll();
    }

    private BorrowingRequest getRequest(UUID id) {
        return borrowingRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + id));
    }
}
