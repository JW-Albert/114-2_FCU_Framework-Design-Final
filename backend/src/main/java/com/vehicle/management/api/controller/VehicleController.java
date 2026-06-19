package com.vehicle.management.api.controller;

import com.vehicle.management.api.dto.CreateVehicleRequest;
import com.vehicle.management.api.dto.VehicleResponse;
import com.vehicle.management.domain.model.User;
import com.vehicle.management.service.UserService;
import com.vehicle.management.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    private final UserService userService;

    public VehicleController(VehicleService vehicleService, UserService userService) {
        this.vehicleService = vehicleService;
        this.userService = userService;
    }

    @GetMapping
    public List<VehicleResponse> listAll() {
        return vehicleService.listAll().stream().map(VehicleResponse::from).toList();
    }

    @GetMapping("/available")
    public List<VehicleResponse> findAvailable(@RequestParam Instant start,
                                                @RequestParam Instant end) {
        return vehicleService.findAvailable(start, end).stream().map(VehicleResponse::from).toList();
    }

    @GetMapping("/{id}")
    public VehicleResponse getOne(@PathVariable UUID id) {
        return VehicleResponse.from(vehicleService.getVehicle(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponse create(@AuthenticationPrincipal UserDetails principal,
                                   @Valid @RequestBody CreateVehicleRequest req) {
        User actor = userService.findByEmail(principal.getUsername());
        return VehicleResponse.from(
                vehicleService.createVehicle(actor, req.plate(), req.model(), req.year()));
    }

    @PutMapping("/{id}")
    public VehicleResponse update(@AuthenticationPrincipal UserDetails principal,
                                   @PathVariable UUID id,
                                   @Valid @RequestBody CreateVehicleRequest req) {
        User actor = userService.findByEmail(principal.getUsername());
        return VehicleResponse.from(
                vehicleService.updateVehicle(actor, id, req.plate(), req.model(), req.year()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserDetails principal,
                       @PathVariable UUID id) {
        User actor = userService.findByEmail(principal.getUsername());
        vehicleService.deleteVehicle(actor, id);
    }

    /** 查詢已軟刪除的車輛（管理員資料保留檢視）。 */
    @GetMapping("/deleted")
    public List<VehicleResponse> listDeleted(@AuthenticationPrincipal UserDetails principal) {
        User actor = userService.findByEmail(principal.getUsername());
        return vehicleService.listDeleted(actor).stream().map(VehicleResponse::from).toList();
    }

    /** 還原已軟刪除的車輛。 */
    @PostMapping("/{id}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restore(@AuthenticationPrincipal UserDetails principal,
                        @PathVariable UUID id) {
        User actor = userService.findByEmail(principal.getUsername());
        vehicleService.restoreVehicle(actor, id);
    }
}
