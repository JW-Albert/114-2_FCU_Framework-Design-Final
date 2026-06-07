package com.vehicle.management.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vehicle.management.api.controller.BorrowingController;
import com.vehicle.management.api.controller.GlobalExceptionHandler;
import com.vehicle.management.domain.model.BorrowingRequest;
import com.vehicle.management.domain.model.User;
import com.vehicle.management.domain.role.RoleFactory;
import com.vehicle.management.infrastructure.security.JwtAuthFilter;
import com.vehicle.management.infrastructure.security.JwtUtil;
import com.vehicle.management.infrastructure.security.UserDetailsServiceImpl;
import com.vehicle.management.service.BorrowingService;
import com.vehicle.management.service.PermissionDeniedException;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = { BorrowingController.class, GlobalExceptionHandler.class })
@Import({ JwtAuthFilter.class, JwtUtil.class })
class BorrowingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BorrowingService borrowingService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private User employee;
    private User admin;
    private BorrowingRequest sampleRequest;

    @BeforeEach
    void setUp() {
        employee = new User(UUID.randomUUID(), "Alice", "alice@test.com",
                "hash", Set.of(RoleFactory.create("EMPLOYEE")), Instant.now());
        admin = new User(UUID.randomUUID(), "Bob", "bob@test.com",
                "hash", Set.of(RoleFactory.create("ADMIN")), Instant.now());

        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = start.plus(2, ChronoUnit.HOURS);
        sampleRequest = new BorrowingRequest(
                UUID.randomUUID(), employee.getId(), UUID.randomUUID(),
                start, end, "Business trip", Instant.now());
    }

    @Test
    @WithMockUser(username = "alice@test.com")
    void submitRequest_returnsCreated() throws Exception {
        when(userService.findByEmail("alice@test.com")).thenReturn(employee);
        when(borrowingService.submitRequest(any(), any(), any(), any(), anyString()))
                .thenReturn(sampleRequest);

        Map<String, Object> body = Map.of(
                "vehicleId", sampleRequest.getVehicleId().toString(),
                "periodStart", sampleRequest.getPeriodStart().toString(),
                "periodEnd", sampleRequest.getPeriodEnd().toString(),
                "purpose", "Business trip"
        );

        mockMvc.perform(post("/api/borrowings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.state").value("PENDING"))
                .andExpect(jsonPath("$.purpose").value("Business trip"));
    }

    @Test
    @WithMockUser(username = "alice@test.com")
    void listPending_returnsOk() throws Exception {
        when(borrowingService.listPending()).thenReturn(List.of(sampleRequest));

        mockMvc.perform(get("/api/borrowings/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(username = "bob@test.com")
    void approveRequest_returnsOk() throws Exception {
        when(userService.findByEmail("bob@test.com")).thenReturn(admin);
        sampleRequest.approve("Approved");
        when(borrowingService.approveRequest(any(), any(), any())).thenReturn(sampleRequest);

        mockMvc.perform(post("/api/borrowings/" + sampleRequest.getId() + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"OK\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("APPROVED"));
    }

    @Test
    @WithMockUser(username = "alice@test.com")
    void approveRequest_forbiddenForEmployee() throws Exception {
        when(userService.findByEmail("alice@test.com")).thenReturn(employee);
        when(borrowingService.approveRequest(any(), any(), any()))
                .thenThrow(new PermissionDeniedException("not allowed"));

        mockMvc.perform(post("/api/borrowings/" + sampleRequest.getId() + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void submitRequest_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/borrowings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
