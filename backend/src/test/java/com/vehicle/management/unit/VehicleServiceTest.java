package com.vehicle.management.unit;

import com.vehicle.management.domain.model.User;
import com.vehicle.management.domain.model.Vehicle;
import com.vehicle.management.domain.role.RoleFactory;
import com.vehicle.management.repository.inmemory.InMemoryVehicleRepository;
import com.vehicle.management.service.PermissionDeniedException;
import com.vehicle.management.service.ResourceNotFoundException;
import com.vehicle.management.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class VehicleServiceTest {

    private VehicleService service;
    private User admin;
    private User employee;

    @BeforeEach
    void setUp() {
        service = new VehicleService(new InMemoryVehicleRepository());
        admin = new User(UUID.randomUUID(), "Admin", "admin@example.com",
                "hash", Set.of(RoleFactory.create("ADMIN")), Instant.now());
        employee = new User(UUID.randomUUID(), "Emp", "emp@example.com",
                "hash", Set.of(RoleFactory.create("EMPLOYEE")), Instant.now());
    }

    @Test
    void adminCanCreateVehicle() {
        Vehicle v = service.createVehicle(admin, "ABC-001", "Toyota", 2023);
        assertThat(v.getId()).isNotNull();
        assertThat(v.getPlate()).isEqualTo("ABC-001");
    }

    @Test
    void employeeCannotCreateVehicle() {
        assertThatThrownBy(() -> service.createVehicle(employee, "ABC-002", "Honda", 2022))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    void listAllReturnsCreatedVehicles() {
        service.createVehicle(admin, "P-001", "BMW", 2021);
        service.createVehicle(admin, "P-002", "Audi", 2022);
        List<Vehicle> all = service.listAll();
        assertThat(all).hasSize(2);
    }

    @Test
    void getVehicleThrowsForUnknownId() {
        assertThatThrownBy(() -> service.getVehicle(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void adminCanDeleteVehicle() {
        Vehicle v = service.createVehicle(admin, "DEL-001", "Ford", 2020);
        service.deleteVehicle(admin, v.getId());
        assertThatThrownBy(() -> service.getVehicle(v.getId()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
