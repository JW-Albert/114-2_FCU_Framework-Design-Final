package com.vehicle.management.unit;

import com.vehicle.management.domain.model.User;
import com.vehicle.management.repository.inmemory.InMemoryUserRepository;
import com.vehicle.management.service.ConflictException;
import com.vehicle.management.service.ResourceNotFoundException;
import com.vehicle.management.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.*;

class UserServiceTest {

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(new InMemoryUserRepository(), new BCryptPasswordEncoder());
    }

    @Test
    void registerCreatesUser() {
        User user = service.register("Alice", "alice@test.com", "secret123", "EMPLOYEE");
        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo("alice@test.com");
        assertThat(user.getName()).isEqualTo("Alice");
    }

    @Test
    void registerHashesPassword() {
        User user = service.register("Bob", "bob@test.com", "plainpwd", "EMPLOYEE");
        assertThat(user.getPasswordHash()).isNotEqualTo("plainpwd");
        assertThat(service.verifyPassword(user, "plainpwd")).isTrue();
        assertThat(service.verifyPassword(user, "wrong")).isFalse();
    }

    @Test
    void duplicateEmailThrows() {
        service.register("Alice", "dup@test.com", "pass", "EMPLOYEE");
        assertThatThrownBy(() -> service.register("Alice2", "dup@test.com", "pass", "EMPLOYEE"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void findByEmailWorks() {
        service.register("Carol", "carol@test.com", "pwd", "ADMIN");
        User found = service.findByEmail("carol@test.com");
        assertThat(found.getName()).isEqualTo("Carol");
    }

    @Test
    void findByEmailThrowsForUnknown() {
        assertThatThrownBy(() -> service.findByEmail("unknown@test.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void adminRoleGrantsApprovePermission() {
        User admin = service.register("Admin", "adm@test.com", "pwd", "ADMIN");
        assertThat(admin.can(com.vehicle.management.domain.role.Permission.APPROVE_BORROWING)).isTrue();
        assertThat(admin.can(com.vehicle.management.domain.role.Permission.MANAGE_VEHICLE)).isTrue();
    }

    @Test
    void employeeRoleCanOnlySubmit() {
        User emp = service.register("Emp", "emp@test.com", "pwd", "EMPLOYEE");
        assertThat(emp.can(com.vehicle.management.domain.role.Permission.SUBMIT_REQUEST)).isTrue();
        assertThat(emp.can(com.vehicle.management.domain.role.Permission.APPROVE_BORROWING)).isFalse();
    }
}
