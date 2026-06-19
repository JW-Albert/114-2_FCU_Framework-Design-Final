package com.vehicle.management.api.controller;

import com.vehicle.management.api.dto.BorrowingResponse;
import com.vehicle.management.api.dto.CompleteBorrowingRequest;
import com.vehicle.management.api.dto.ConflictCheckResponse;
import com.vehicle.management.api.dto.ReviewBorrowingRequest;
import com.vehicle.management.api.dto.SubmitBorrowingRequest;
import com.vehicle.management.api.dto.UpdateBorrowingDetailsRequest;
import com.vehicle.management.domain.command.ApproveCommand;
import com.vehicle.management.domain.command.CompleteCommand;
import com.vehicle.management.domain.command.RejectCommand;
import com.vehicle.management.domain.command.RevokeCommand;
import com.vehicle.management.domain.command.StartUseCommand;
import com.vehicle.management.domain.model.BorrowingRequest;
import com.vehicle.management.domain.model.User;
import com.vehicle.management.service.BorrowingCommandBus;
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
    private final BorrowingCommandBus commandBus;
    private final UserService userService;

    public BorrowingController(BorrowingService borrowingService,
                               BorrowingCommandBus commandBus,
                               UserService userService) {
        this.borrowingService = borrowingService;
        this.commandBus = commandBus;
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

    /**
     * 月曆視圖用端點：查詢 [start, end] 時段內的借用記錄（不含已拒絕）。
     *
     * @param start ISO-8601 時間戳（開始）
     * @param end   ISO-8601 時間戳（結束）
     * @return 時段內的借用申請清單
     */
    @GetMapping("/calendar")
    public ResponseEntity<List<BorrowingResponse>> getCalendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        List<BorrowingResponse> result = borrowingService.listInRange(start, end)
                .stream().map(BorrowingResponse::from).toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/approve")
    public BorrowingResponse approve(@AuthenticationPrincipal UserDetails principal,
                                      @PathVariable UUID id,
                                      @RequestBody(required = false) ReviewBorrowingRequest req) {
        User actor = userService.findByEmail(principal.getUsername());
        String note = req != null ? req.note() : null;
        return BorrowingResponse.from(commandBus.dispatch(new ApproveCommand(borrowingService, actor, id, note)));
    }

    @PostMapping("/{id}/reject")
    public BorrowingResponse reject(@AuthenticationPrincipal UserDetails principal,
                                     @PathVariable UUID id,
                                     @RequestBody(required = false) ReviewBorrowingRequest req) {
        User actor = userService.findByEmail(principal.getUsername());
        String note = req != null ? req.note() : null;
        return BorrowingResponse.from(commandBus.dispatch(new RejectCommand(borrowingService, actor, id, note)));
    }

    /** 撤銷核准（APPROVED → PENDING），管理員 / 主管適用。 */
    @PostMapping("/{id}/revoke")
    public BorrowingResponse revoke(@AuthenticationPrincipal UserDetails principal,
                                     @PathVariable UUID id,
                                     @RequestBody(required = false) ReviewBorrowingRequest req) {
        User actor = userService.findByEmail(principal.getUsername());
        String note = req != null ? req.note() : null;
        return BorrowingResponse.from(commandBus.dispatch(new RevokeCommand(borrowingService, actor, id, note)));
    }

    /** 更改申請的借用時段與事由，管理員 / 主管適用。 */
    @PutMapping("/{id}/details")
    public BorrowingResponse updateDetails(@AuthenticationPrincipal UserDetails principal,
                                            @PathVariable UUID id,
                                            @Valid @RequestBody UpdateBorrowingDetailsRequest req) {
        User actor = userService.findByEmail(principal.getUsername());
        return BorrowingResponse.from(borrowingService.updateRequestDetails(
                actor, id, req.periodStart(), req.periodEnd(), req.purpose()));
    }

    @PostMapping("/{id}/start")
    public BorrowingResponse startUse(@PathVariable UUID id) {
        return BorrowingResponse.from(commandBus.dispatch(new StartUseCommand(borrowingService, id)));
    }

    @PostMapping("/{id}/complete")
    public BorrowingResponse complete(@PathVariable UUID id,
                                       @Valid @RequestBody CompleteBorrowingRequest req) {
        return BorrowingResponse.from(commandBus.dispatch(new CompleteCommand(borrowingService, id, req.endMileage())));
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
