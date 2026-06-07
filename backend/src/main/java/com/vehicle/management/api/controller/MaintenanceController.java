package com.vehicle.management.api.controller;

import com.vehicle.management.api.dto.AddMaintenanceRequest;
import com.vehicle.management.api.dto.MaintenanceResponse;
import com.vehicle.management.domain.model.User;
import com.vehicle.management.service.MaintenanceService;
import com.vehicle.management.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;
    private final UserService userService;

    public MaintenanceController(MaintenanceService maintenanceService, UserService userService) {
        this.maintenanceService = maintenanceService;
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceResponse addRecord(@AuthenticationPrincipal UserDetails principal,
                                          @Valid @RequestBody AddMaintenanceRequest req) {
        User actor = userService.findByEmail(principal.getUsername());
        return MaintenanceResponse.from(maintenanceService.addRecord(
                actor, req.vehicleId(), req.date(), req.items(),
                req.cost(), req.nextDueDate(), req.nextDueKm()));
    }

    @GetMapping("/vehicle/{vehicleId}")
    public List<MaintenanceResponse> getByVehicle(@PathVariable UUID vehicleId) {
        return maintenanceService.getRecords(vehicleId).stream()
                .map(MaintenanceResponse::from).toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserDetails principal,
                       @PathVariable UUID id) {
        User actor = userService.findByEmail(principal.getUsername());
        maintenanceService.deleteRecord(actor, id);
    }
}
