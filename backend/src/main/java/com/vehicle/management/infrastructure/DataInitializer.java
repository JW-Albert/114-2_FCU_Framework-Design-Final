package com.vehicle.management.infrastructure;

import com.vehicle.management.domain.model.User;
import com.vehicle.management.domain.model.Vehicle;
import com.vehicle.management.domain.model.VehicleStatus;
import com.vehicle.management.repository.IUserRepository;
import com.vehicle.management.repository.IVehicleRepository;
import com.vehicle.management.service.UserService;
import com.vehicle.management.service.VehicleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/** dev profile 啟動時自動植入測試帳號與車輛資料。 */
@Component
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserService userService;
    private final IUserRepository userRepo;
    private final IVehicleRepository vehicleRepo;

    public DataInitializer(UserService userService,
                           IUserRepository userRepo,
                           IVehicleRepository vehicleRepo) {
        this.userService = userService;
        this.userRepo = userRepo;
        this.vehicleRepo = vehicleRepo;
    }

    @Override
    public void run(String... args) {
        if (userRepo.existsByEmail("admin@demo.com")) return;

        // 先建立管理員，再以管理員身分建立帶部門的主管與員工帳號
        // （createUserByAdmin 需要 MANAGE_USER 權限，且支援指定部門）
        User admin = userService.register("管理員", "admin@demo.com", "Admin1234", "ADMIN");
        userService.createUserByAdmin(admin, "主管", "manager@demo.com", "Manager1234", "MANAGER", "測試部門");
        userService.createUserByAdmin(admin, "員工甲", "emp@demo.com", "Emp1234", "EMPLOYEE", "測試部門");
        log.info(">>> [DEV] 已建立測試帳號：admin@demo.com / manager@demo.com（測試部門）/ emp@demo.com（測試部門）");

        vehicleRepo.save(new Vehicle(UUID.randomUUID(), "ABC-1234", "Toyota Camry", 2022,
                VehicleStatus.AVAILABLE, Instant.now()));
        vehicleRepo.save(new Vehicle(UUID.randomUUID(), "XYZ-5678", "Honda Fit", 2021,
                VehicleStatus.AVAILABLE, Instant.now()));
        vehicleRepo.save(new Vehicle(UUID.randomUUID(), "DEF-9012", "Mazda CX-5", 2023,
                VehicleStatus.AVAILABLE, Instant.now()));
        log.info(">>> [DEV] 已建立 3 輛測試車輛");
    }
}
