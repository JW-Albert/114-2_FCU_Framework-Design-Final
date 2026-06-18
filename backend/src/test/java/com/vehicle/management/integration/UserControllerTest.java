package com.vehicle.management.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vehicle.management.api.controller.GlobalExceptionHandler;
import com.vehicle.management.api.controller.UserController;
import com.vehicle.management.domain.model.User;
import com.vehicle.management.domain.role.RoleFactory;
import com.vehicle.management.infrastructure.security.JwtAuthFilter;
import com.vehicle.management.infrastructure.security.JwtUtil;
import com.vehicle.management.infrastructure.security.SecurityConfig;
import com.vehicle.management.infrastructure.security.UserDetailsServiceImpl;
import com.vehicle.management.service.PermissionDeniedException;
import com.vehicle.management.service.ResourceNotFoundException;
import com.vehicle.management.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = { UserController.class, GlobalExceptionHandler.class })
@Import({ SecurityConfig.class, JwtAuthFilter.class, JwtUtil.class })
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private User admin;
    private User employee;
    private User targetUser;

    @BeforeEach
    void setUp() {
        admin = new User(UUID.randomUUID(), "Admin", "admin@test.com",
                "hash", Set.of(RoleFactory.create("ADMIN")), Instant.now());
        employee = new User(UUID.randomUUID(), "Alice", "alice@test.com",
                "hash", Set.of(RoleFactory.create("EMPLOYEE")), Instant.now());
        targetUser = new User(UUID.randomUUID(), "Target", "target@test.com",
                "hash", Set.of(RoleFactory.create("EMPLOYEE")), Instant.now());
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    void listAll_adminReturns200() throws Exception {
        when(userService.findByEmail("admin@test.com")).thenReturn(admin);
        when(userService.listAllUsers(admin)).thenReturn(List.of(admin, employee));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(username = "alice@test.com")
    void listAll_employeeReturns403() throws Exception {
        when(userService.findByEmail("alice@test.com")).thenReturn(employee);
        when(userService.listAllUsers(employee))
                .thenThrow(new PermissionDeniedException("Only admins can manage user accounts"));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listAll_unauthenticatedReturns401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    void getOne_adminReturns200() throws Exception {
        when(userService.findByEmail("admin@test.com")).thenReturn(admin);
        when(userService.getUserById(admin, targetUser.getId())).thenReturn(targetUser);

        mockMvc.perform(get("/api/users/" + targetUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("target@test.com"));
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    void getOne_notFoundReturns404() throws Exception {
        UUID missing = UUID.randomUUID();
        when(userService.findByEmail("admin@test.com")).thenReturn(admin);
        when(userService.getUserById(admin, missing))
                .thenThrow(new ResourceNotFoundException("User not found: " + missing));

        mockMvc.perform(get("/api/users/" + missing))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    void createUser_adminReturns201() throws Exception {
        when(userService.findByEmail("admin@test.com")).thenReturn(admin);
        when(userService.createUserByAdmin(eq(admin), anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(targetUser);

        Map<String, String> body = Map.of(
                "name", "Target",
                "email", "target@test.com",
                "password", "Pass1234",
                "role", "EMPLOYEE"
        );

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("target@test.com"));
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    void updateUser_adminReturns200() throws Exception {
        User updated = new User(targetUser.getId(), "Updated", "updated@test.com",
                "hash", Set.of(RoleFactory.create("EMPLOYEE")), targetUser.getCreatedAt());
        when(userService.findByEmail("admin@test.com")).thenReturn(admin);
        when(userService.updateUser(eq(admin), eq(targetUser.getId()), anyString(), anyString(), any()))
                .thenReturn(updated);

        Map<String, String> body = Map.of("name", "Updated", "email", "updated@test.com");

        mockMvc.perform(put("/api/users/" + targetUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    void changeRole_adminReturns200() throws Exception {
        User promoted = new User(targetUser.getId(), targetUser.getName(), targetUser.getEmail(),
                "hash", Set.of(RoleFactory.create("ADMIN")), targetUser.getCreatedAt());
        when(userService.findByEmail("admin@test.com")).thenReturn(admin);
        when(userService.changeUserRole(eq(admin), eq(targetUser.getId()), eq("ADMIN")))
                .thenReturn(promoted);

        mockMvc.perform(patch("/api/users/" + targetUser.getId() + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"));
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    void deleteUser_adminReturns204() throws Exception {
        when(userService.findByEmail("admin@test.com")).thenReturn(admin);
        doNothing().when(userService).deleteUser(admin, targetUser.getId());

        mockMvc.perform(delete("/api/users/" + targetUser.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin@test.com")
    void deleteUser_notFoundReturns404() throws Exception {
        UUID missing = UUID.randomUUID();
        when(userService.findByEmail("admin@test.com")).thenReturn(admin);
        doThrow(new ResourceNotFoundException("User not found: " + missing))
                .when(userService).deleteUser(admin, missing);

        mockMvc.perform(delete("/api/users/" + missing))
                .andExpect(status().isNotFound());
    }
}
