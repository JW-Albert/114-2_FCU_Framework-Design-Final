package com.vehicle.management.unit;

import com.vehicle.management.domain.model.MaintenanceRecord;
import com.vehicle.management.domain.model.User;
import com.vehicle.management.domain.role.RoleFactory;
import com.vehicle.management.repository.inmemory.InMemoryMaintenanceRepository;
import com.vehicle.management.service.MaintenanceService;
import com.vehicle.management.service.PermissionDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class MaintenanceServiceTest {

    private MaintenanceService service;
    private User admin;
    private User employee;

    @BeforeEach
    void setUp() {
        service = new MaintenanceService(new InMemoryMaintenanceRepository());
        admin = new User(UUID.randomUUID(), "Admin", "admin@test.com",
                "hash", Set.of(RoleFactory.create("ADMIN")), Instant.now());
        employee = new User(UUID.randomUUID(), "Emp", "emp@test.com",
                "hash", Set.of(RoleFactory.create("EMPLOYEE")), Instant.now());
    }

    @Test
    void adminCanAddRecord() {
        UUID vehicleId = UUID.randomUUID();
        MaintenanceRecord record = service.addRecord(
                admin, vehicleId, LocalDate.now(),
                List.of("機油更換", "輪胎檢查"), new BigDecimal("3500"),
                LocalDate.now().plusMonths(6), 5000);

        assertThat(record).isNotNull();
        assertThat(record.getItems()).containsExactlyInAnyOrder("機油更換", "輪胎檢查");
        assertThat(record.getCost()).isEqualByComparingTo("3500");
    }

    @Test
    void employeeCannotAddRecord() {
        assertThatThrownBy(() -> service.addRecord(
                employee, UUID.randomUUID(), LocalDate.now(),
                List.of("機油"), new BigDecimal("1000"), null, null))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    void getRecordsReturnsOnlyForVehicle() {
        UUID v1 = UUID.randomUUID();
        UUID v2 = UUID.randomUUID();
        service.addRecord(admin, v1, LocalDate.now(), List.of("A"), BigDecimal.TEN, null, null);
        service.addRecord(admin, v1, LocalDate.now(), List.of("B"), BigDecimal.TEN, null, null);
        service.addRecord(admin, v2, LocalDate.now(), List.of("C"), BigDecimal.TEN, null, null);

        List<MaintenanceRecord> v1Records = service.getRecords(v1);
        assertThat(v1Records).hasSize(2);
    }

    @Test
    void getDueRemindersReturnsDueByDate() {
        UUID vehicleId = UUID.randomUUID();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        service.addRecord(admin, vehicleId, LocalDate.now().minusMonths(6),
                List.of("換油"), BigDecimal.TEN, yesterday, null);

        List<MaintenanceRecord> due = service.getDueReminders(LocalDate.now(), java.util.Map.of());
        assertThat(due).hasSize(1);
    }

    @Test
    void adminCanDeleteRecord() {
        UUID vehicleId = UUID.randomUUID();
        MaintenanceRecord record = service.addRecord(
                admin, vehicleId, LocalDate.now(), List.of("X"), BigDecimal.ONE, null, null);

        service.deleteRecord(admin, record.getId());
        assertThat(service.getRecords(vehicleId)).isEmpty();
    }
}
