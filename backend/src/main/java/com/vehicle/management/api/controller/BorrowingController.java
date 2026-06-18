package com.vehicle.management.api.controller;

import com.vehicle.management.api.dto.BorrowingResponse;
import com.vehicle.management.api.dto.CompleteBorrowingRequest;
import com.vehicle.management.api.dto.ConflictCheckResponse;
import com.vehicle.management.api.dto.ReviewBorrowingRequest;
import com.vehicle.management.api.dto.SubmitBorrowingRequest;
import com.vehicle.management.domain.model.BorrowingRequest;
import com.vehicle.management.domain.model.User;
import com.vehicle.management.service.BorrowingService;
import com.vehicle.management.service.UserService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/borrowings")
public class BorrowingController {

    private final BorrowingService borrowingService;
    private final UserService userService;

    public BorrowingController(BorrowingService borrowingService, UserService userService) {
        this.borrowingService = borrowingService;
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BorrowingResponse submit(@AuthenticationPrincipal UserDetails principal,
                                     @Valid @RequestBody SubmitBorrowingRequest req) {
        User actor = userService.findByEmail(principal.getUsername());
        return BorrowingResponse.from(borrowingService.submitRequest(
                actor, req.vehicleId(), req.periodStart(), req.periodEnd(), req.purpose()));
    }

    @GetMapping("/pending")
    public List<BorrowingResponse> listPending() {
        return borrowingService.listPending().stream().map(BorrowingResponse::from).toList();
    }

    @GetMapping("/my")
    public List<BorrowingResponse> listMine(@AuthenticationPrincipal UserDetails principal) {
        User actor = userService.findByEmail(principal.getUsername());
        return borrowingService.listMyRequests(actor.getId()).stream()
                .map(BorrowingResponse::from).toList();
    }

    @GetMapping
    public List<BorrowingResponse> listAll() {
        return borrowingService.listAll().stream().map(BorrowingResponse::from).toList();
    }

    @PostMapping("/{id}/approve")
    public BorrowingResponse approve(@AuthenticationPrincipal UserDetails principal,
                                      @PathVariable UUID id,
                                      @RequestBody(required = false) ReviewBorrowingRequest req) {
        User actor = userService.findByEmail(principal.getUsername());
        String note = req != null ? req.note() : null;
        return BorrowingResponse.from(borrowingService.approveRequest(actor, id, note));
    }

    @PostMapping("/{id}/reject")
    public BorrowingResponse reject(@AuthenticationPrincipal UserDetails principal,
                                     @PathVariable UUID id,
                                     @RequestBody(required = false) ReviewBorrowingRequest req) {
        User actor = userService.findByEmail(principal.getUsername());
        String note = req != null ? req.note() : null;
        return BorrowingResponse.from(borrowingService.rejectRequest(actor, id, note));
    }

    @PostMapping("/{id}/start")
    public BorrowingResponse startUse(@PathVariable UUID id) {
        return BorrowingResponse.from(borrowingService.startUse(id));
    }

    @PostMapping("/{id}/complete")
    public BorrowingResponse complete(@PathVariable UUID id,
                                       @Valid @RequestBody CompleteBorrowingRequest req) {
        return BorrowingResponse.from(borrowingService.completeUse(id, req.endMileage()));
    }

    @GetMapping("/conflict-check")
    public ResponseEntity<ConflictCheckResponse> checkConflict(
            @RequestParam UUID vehicleId,
            @RequestParam @DateTimeFormat(iso = ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = ISO.DATE_TIME) Instant end,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<BorrowingRequest> conflicts = borrowingService.findConflicts(vehicleId, start, end);
        return ResponseEntity.ok(new ConflictCheckResponse(
                !conflicts.isEmpty(),
                conflicts.stream().map(BorrowingResponse::from).toList()));
    }
}
