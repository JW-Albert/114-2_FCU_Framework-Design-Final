package com.vehicle.management.unit;

import com.vehicle.management.domain.model.BorrowingRequest;
import com.vehicle.management.domain.model.User;
import com.vehicle.management.domain.model.Vehicle;
import com.vehicle.management.domain.model.VehicleStatus;
import com.vehicle.management.domain.role.RoleFactory;
import com.vehicle.management.repository.inmemory.InMemoryBorrowingRepository;
import com.vehicle.management.repository.inmemory.InMemoryVehicleRepository;
import com.vehicle.management.service.BorrowingService;
import com.vehicle.management.service.ConflictException;
import com.vehicle.management.service.PermissionDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class BorrowingServiceTest {

    private BorrowingService service;
    private InMemoryVehicleRepository vehicleRepo;
    private User employee;
    private User admin;
    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        vehicleRepo = new InMemoryVehicleRepository();
        InMemoryBorrowingRepository borrowingRepo = new InMemoryBorrowingRepository();
        service = new BorrowingService(borrowingRepo, vehicleRepo);

        employee = new User(UUID.randomUUID(), "Alice", "alice@example.com",
                "hash", Set.of(RoleFactory.create("EMPLOYEE")), Instant.now());
        admin = new User(UUID.randomUUID(), "Bob", "bob@example.com",
                "hash", Set.of(RoleFactory.create("ADMIN")), Instant.now());
        vehicle = new Vehicle(UUID.randomUUID(), "ABC-1234", "Toyota Camry", 2022,
                VehicleStatus.AVAILABLE, Instant.now());
        vehicleRepo.save(vehicle);
    }

    @Test
    void employeeCanSubmitRequest() {
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = start.plus(2, ChronoUnit.HOURS);

        BorrowingRequest req = service.submitRequest(employee, vehicle.getId(), start, end, "Business trip");

        assertThat(req).isNotNull();
        assertThat(req.getStateName()).isEqualTo("PENDING");
        assertThat(req.getUserId()).isEqualTo(employee.getId());
    }

    @Test
    void adminCanApproveRequest() {
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = start.plus(2, ChronoUnit.HOURS);
        BorrowingRequest req = service.submitRequest(employee, vehicle.getId(), start, end, "Trip");

        BorrowingRequest approved = service.approveRequest(admin, req.getId(), "OK");

        assertThat(approved.getStateName()).isEqualTo("APPROVED");
        assertThat(approved.getReviewNote()).isEqualTo("OK");
    }

    @Test
    void adminCanRejectRequest() {
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = start.plus(2, ChronoUnit.HOURS);
        BorrowingRequest req = service.submitRequest(employee, vehicle.getId(), start, end, "Trip");

        BorrowingRequest rejected = service.rejectRequest(admin, req.getId(), "Not available");

        assertThat(rejected.getStateName()).isEqualTo("REJECTED");
    }

    @Test
    void employeeCannotApprove() {
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = start.plus(2, ChronoUnit.HOURS);
        BorrowingRequest req = service.submitRequest(employee, vehicle.getId(), start, end, "Trip");

        assertThatThrownBy(() -> service.approveRequest(employee, req.getId(), null))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    void conflictingRequestThrows() {
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = start.plus(4, ChronoUnit.HOURS);
        service.submitRequest(employee, vehicle.getId(), start, end, "First trip");

        assertThatThrownBy(() -> service.submitRequest(employee, vehicle.getId(),
                start.plus(1, ChronoUnit.HOURS), end.minus(1, ChronoUnit.HOURS), "Overlap"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void fullWorkflowPendingToReturned() {
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = start.plus(2, ChronoUnit.HOURS);
        BorrowingRequest req = service.submitRequest(employee, vehicle.getId(), start, end, "Trip");

        service.approveRequest(admin, req.getId(), null);
        service.startUse(req.getId());
        BorrowingRequest completed = service.completeUse(req.getId());

        assertThat(completed.getStateName()).isEqualTo("RETURNED");
    }
}
