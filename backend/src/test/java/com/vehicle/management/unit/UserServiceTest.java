package com.vehicle.management.unit;

import com.vehicle.management.domain.model.User;
import com.vehicle.management.repository.inmemory.InMemoryUserRepository;
import com.vehicle.management.service.ConflictException;
import com.vehicle.management.service.PermissionDeniedException;
import com.vehicle.management.service.ResourceNotFoundException;
import com.vehicle.management.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class UserServiceTest {

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(new InMemoryUserRepository(), new BCryptPasswordEncoder());
    }

    @Test
    void registerCreatesUser() {
        User user = service.register("Alice", "alice@test.com", "Secret123", "EMPLOYEE");
        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo("alice@test.com");
        assertThat(user.getName()).isEqualTo("Alice");
    }

    @Test
    void registerHashesPassword() {
        User user = service.register("Bob", "bob@test.com", "Plainpwd1", "EMPLOYEE");
        assertThat(user.getPasswordHash()).isNotEqualTo("Plainpwd1");
        assertThat(service.verifyPassword(user, "Plainpwd1")).isTrue();
        assertThat(service.verifyPassword(user, "wrong")).isFalse();
    }

    @Test
    void duplicateEmailThrows() {
        service.register("Alice", "dup@test.com", "Passw0rd", "EMPLOYEE");
        assertThatThrownBy(() -> service.register("Alice2", "dup@test.com", "Passw0rd", "EMPLOYEE"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void findByEmailWorks() {
        service.register("Carol", "carol@test.com", "Passw0rd", "ADMIN");
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
        User admin = service.register("Admin", "adm@test.com", "Passw0rd", "ADMIN");
        assertThat(admin.can(com.vehicle.management.domain.role.Permission.APPROVE_BORROWING)).isTrue();
        assertThat(admin.can(com.vehicle.management.domain.role.Permission.MANAGE_VEHICLE)).isTrue();
    }

    @Test
    void employeeRoleCanOnlySubmit() {
        User emp = service.register("Emp", "emp@test.com", "Passw0rd", "EMPLOYEE");
        assertThat(emp.can(com.vehicle.management.domain.role.Permission.SUBMIT_REQUEST)).isTrue();
        assertThat(emp.can(com.vehicle.management.domain.role.Permission.APPROVE_BORROWING)).isFalse();
    }

    @Test
    void adminRoleGrantsManageUserPermission() {
        User admin = service.register("Admin", "admin@test.com", "Passw0rd", "ADMIN");
        assertThat(admin.can(com.vehicle.management.domain.role.Permission.MANAGE_USER)).isTrue();
    }

    @Test
    void employeeCannotManageUsers() {
        User emp = service.register("Emp", "emp@test.com", "Passw0rd", "EMPLOYEE");
        assertThat(emp.can(com.vehicle.management.domain.role.Permission.MANAGE_USER)).isFalse();
    }

    @Test
    void adminCanListAllUsers() {
        User admin = service.register("Admin", "admin@test.com", "Passw0rd", "ADMIN");
        service.register("User1", "u1@test.com", "Passw0rd", "EMPLOYEE");
        service.register("User2", "u2@test.com", "Passw0rd", "EMPLOYEE");
        List<User> users = service.listAllUsers(admin);
        assertThat(users).hasSize(3);
    }

    @Test
    void employeeCannotListUsers() {
        User emp = service.register("Emp", "emp@test.com", "Passw0rd", "EMPLOYEE");
        assertThatThrownBy(() -> service.listAllUsers(emp))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    void adminCanGetUserById() {
        User admin = service.register("Admin", "admin@test.com", "Passw0rd", "ADMIN");
        User target = service.register("Target", "target@test.com", "Passw0rd", "EMPLOYEE");
        User found = service.getUserById(admin, target.getId());
        assertThat(found.getEmail()).isEqualTo("target@test.com");
    }

    @Test
    void adminCanCreateUserByAdmin() {
        User admin = service.register("Admin", "admin@test.com", "Passw0rd", "ADMIN");
        User created = service.createUserByAdmin(admin, "New", "new@test.com", "Passw0rd", "EMPLOYEE");
        assertThat(created.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    void adminCanUpdateUser() {
        User admin = service.register("Admin", "admin@test.com", "Passw0rd", "ADMIN");
        User target = service.register("Old Name", "old@test.com", "Passw0rd", "EMPLOYEE");
        User updated = service.updateUser(admin, target.getId(), "New Name", "new@test.com");
        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    void updateUserEmailConflictThrows() {
        User admin = service.register("Admin", "admin@test.com", "Passw0rd", "ADMIN");
        service.register("Other", "other@test.com", "Passw0rd", "EMPLOYEE");
        User target = service.register("Target", "target@test.com", "Passw0rd", "EMPLOYEE");
        assertThatThrownBy(() -> service.updateUser(admin, target.getId(), "Target", "other@test.com"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void adminCanChangeUserRole() {
        User admin = service.register("Admin", "admin@test.com", "Passw0rd", "ADMIN");
        User emp = service.register("Emp", "emp@test.com", "Passw0rd", "EMPLOYEE");
        User updated = service.changeUserRole(admin, emp.getId(), "ADMIN");
        assertThat(updated.can(com.vehicle.management.domain.role.Permission.MANAGE_USER)).isTrue();
    }

    @Test
    void adminCanDeleteUser() {
        User admin = service.register("Admin", "admin@test.com", "Passw0rd", "ADMIN");
        User target = service.register("Target", "target@test.com", "Passw0rd", "EMPLOYEE");
        service.deleteUser(admin, target.getId());
        assertThatThrownBy(() -> service.findByEmail("target@test.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void employeeCannotDeleteUser() {
        User emp = service.register("Emp", "emp@test.com", "Passw0rd", "EMPLOYEE");
        UUID randomId = UUID.randomUUID();
        assertThatThrownBy(() -> service.deleteUser(emp, randomId))
                .isInstanceOf(PermissionDeniedException.class);
    }

    @Test
    void deleteNonExistentUserThrows() {
        User admin = service.register("Admin", "admin@test.com", "Passw0rd", "ADMIN");
        assertThatThrownBy(() -> service.deleteUser(admin, UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
