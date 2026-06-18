package com.vehicle.management.api.controller;

import com.vehicle.management.api.dto.MonthlyStatsResponse;
import com.vehicle.management.api.dto.StatsOverviewResponse;
import com.vehicle.management.api.dto.UserActivityResponse;
import com.vehicle.management.api.dto.VehicleUtilizationResponse;
import com.vehicle.management.domain.model.BorrowingRequest;
import com.vehicle.management.domain.model.User;
import com.vehicle.management.domain.model.Vehicle;
import com.vehicle.management.domain.model.VehicleStatus;
import com.vehicle.management.repository.IBorrowingRepository;
import com.vehicle.management.repository.IUserRepository;
import com.vehicle.management.repository.IVehicleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final IBorrowingRepository borrowingRepo;
    private final IVehicleRepository vehicleRepo;
    private final IUserRepository userRepo;

    public StatsController(IBorrowingRepository borrowingRepo,
                           IVehicleRepository vehicleRepo,
                           IUserRepository userRepo) {
        this.borrowingRepo = borrowingRepo;
        this.vehicleRepo = vehicleRepo;
        this.userRepo = userRepo;
    }

    /**
     * GET /api/stats/overview
     * 回傳系統總覽 KPI：車輛數、可用車輛、待審核、使用中、總使用者數。
     */
    @GetMapping("/overview")
    public ResponseEntity<StatsOverviewResponse> overview() {
        List<Vehicle> vehicles = vehicleRepo.findAll();
        List<BorrowingRequest> borrowings = borrowingRepo.findAll();
        List<User> users = userRepo.findAll();

        long totalVehicles = vehicles.size();
        long availableVehicles = vehicles.stream()
                .filter(v -> v.getStatus() == VehicleStatus.AVAILABLE)
                .count();
        long pendingRequests = borrowings.stream()
                .filter(b -> "PENDING".equals(b.getStateName()))
                .count();
        long activeUses = borrowings.stream()
                .filter(b -> "IN_USE".equals(b.getStateName()))
                .count();
        long totalUsers = users.size();

        return ResponseEntity.ok(new StatsOverviewResponse(
                totalVehicles, availableVehicles, pendingRequests, activeUses, totalUsers));
    }

    /**
     * GET /api/stats/monthly
     * 回傳近 12 個月每月借用申請數量（以 createdAt 為基準）。
     */
    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlyStatsResponse>> monthly() {
        List<BorrowingRequest> borrowings = borrowingRepo.findAll();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM").withZone(ZoneId.systemDefault());

        // 建立近 12 個月的月份清單（含零值）
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        List<String> months = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            months.add(now.minusMonths(i).format(formatter));
        }

        // 依月份統計
        Map<String, Long> countMap = borrowings.stream()
                .collect(Collectors.groupingBy(
                        b -> formatter.format(b.getCreatedAt()),
                        Collectors.counting()
                ));

        List<MonthlyStatsResponse> result = months.stream()
                .map(m -> new MonthlyStatsResponse(m, countMap.getOrDefault(m, 0L).intValue()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/stats/vehicle-utilization
     * 回傳每輛車的使用次數與總天數（只計 RETURNED 與 IN_USE 的申請）。
     */
    @GetMapping("/vehicle-utilization")
    public ResponseEntity<List<VehicleUtilizationResponse>> vehicleUtilization() {
        List<BorrowingRequest> borrowings = borrowingRepo.findAll();
        List<Vehicle> vehicles = vehicleRepo.findAll();

        // 建立 vehicleId → Vehicle 查找表
        Map<UUID, Vehicle> vehicleMap = vehicles.stream()
                .collect(Collectors.toMap(Vehicle::getId, v -> v));

        // 只計算 RETURNED 或 IN_USE 的借用
        List<BorrowingRequest> active = borrowings.stream()
                .filter(b -> "RETURNED".equals(b.getStateName()) || "IN_USE".equals(b.getStateName()))
                .collect(Collectors.toList());

        // 依 vehicleId 分組
        Map<UUID, List<BorrowingRequest>> grouped = active.stream()
                .collect(Collectors.groupingBy(BorrowingRequest::getVehicleId));

        List<VehicleUtilizationResponse> result = grouped.entrySet().stream()
                .filter(e -> vehicleMap.containsKey(e.getKey()))
                .map(e -> {
                    UUID vehicleId = e.getKey();
                    Vehicle vehicle = vehicleMap.get(vehicleId);
                    List<BorrowingRequest> reqs = e.getValue();
                    int usageCount = reqs.size();
                    long totalDays = reqs.stream()
                            .mapToLong(b -> Duration.between(b.getPeriodStart(), b.getPeriodEnd()).toDays())
                            .sum();
                    return new VehicleUtilizationResponse(
                            vehicleId, vehicle.getPlate(), vehicle.getModel(), usageCount, totalDays);
                })
                .sorted(Comparator.comparingInt(VehicleUtilizationResponse::usageCount).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/stats/user-activity
     * 回傳借用申請數前 10 名的使用者。
     */
    @GetMapping("/user-activity")
    public ResponseEntity<List<UserActivityResponse>> userActivity() {
        List<BorrowingRequest> borrowings = borrowingRepo.findAll();
        List<User> users = userRepo.findAll();

        // 建立 userId → User 查找表
        Map<UUID, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 依 userId 統計申請數
        Map<UUID, Long> countMap = borrowings.stream()
                .collect(Collectors.groupingBy(BorrowingRequest::getUserId, Collectors.counting()));

        List<UserActivityResponse> result = countMap.entrySet().stream()
                .filter(e -> userMap.containsKey(e.getKey()))
                .map(e -> {
                    User user = userMap.get(e.getKey());
                    return new UserActivityResponse(e.getKey(), user.getName(), e.getValue().intValue());
                })
                .sorted(Comparator.comparingInt(UserActivityResponse::requestCount).reversed())
                .limit(10)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}
