package com.vehicle.management.service;

import com.vehicle.management.domain.model.BorrowingRequest;
import com.vehicle.management.domain.model.User;
import com.vehicle.management.domain.model.ViolationRecord;
import com.vehicle.management.domain.role.Permission;
import com.vehicle.management.repository.IBorrowingRepository;
import com.vehicle.management.repository.IViolationRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 違規記錄業務邏輯服務。
 *
 * <p>負責兩種違規記錄的建立：
 * <ul>
 *   <li><b>自動</b>：由 {@link BorrowingService#completeUse} 在還車時呼叫
 *       {@link #checkAndCreateOverdue}，偵測超時還車並建立 OVERDUE 記錄。</li>
 *   <li><b>手動</b>：由管理員或部門主管透過 {@link #createManual} 針對某筆借用記錄登錄違規。</li>
 * </ul>
 */
@Service
public class ViolationService {

    private final IViolationRepository violationRepo;
    private final IBorrowingRepository borrowingRepo;

    public ViolationService(IViolationRepository violationRepo,
                            IBorrowingRepository borrowingRepo) {
        this.violationRepo = violationRepo;
        this.borrowingRepo = borrowingRepo;
    }

    /**
     * 手動登錄違規記錄。
     *
     * <p>違規一律關聯到一筆既有借用記錄，違規者（userId）與車輛（vehicleId）
     * 直接自該借用記錄帶出，確保資料一致且滿足非空約束。</p>
     *
     * @param actor       執行登錄的使用者（須具備 {@link Permission#MANAGE_VIOLATION}）
     * @param borrowingId 違規所關聯的借用記錄 ID
     * @param type        違規類型（如 OVERDUE、DAMAGE、ILLEGAL_PARKING、OTHER）
     * @param description 違規描述
     * @return 已儲存的違規記錄
     * @throws PermissionDeniedException 若 actor 不具備登錄違規的權限
     * @throws ResourceNotFoundException 若借用記錄不存在
     */
    public ViolationRecord createManual(User actor, UUID borrowingId,
                                        String type, String description) {
        if (!actor.can(Permission.MANAGE_VIOLATION)) {
            throw new PermissionDeniedException(
                    actor.getEmail() + " cannot create violation records");
        }
        BorrowingRequest borrowing = borrowingRepo.findById(borrowingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Borrowing not found: " + borrowingId));
        ViolationRecord v = new ViolationRecord(
                UUID.randomUUID(),
                borrowing.getUserId(),
                borrowing.getVehicleId(),
                borrowingId,
                type,
                description,
                Instant.now());
        return violationRepo.save(v);
    }

    /**
     * 檢查借車申請是否超時，若超時則自動建立違規記錄。
     *
     * <p>判斷邏輯：若目前時間晚於借車申請的 {@code periodEnd}，則視為超時。</p>
     *
     * @param request 已完成的借車申請
     * @return 若超時則回傳包含違規記錄的 {@link Optional}，否則回傳 {@link Optional#empty()}
     */
    public Optional<ViolationRecord> checkAndCreateOverdue(BorrowingRequest request) {
        Instant now = Instant.now();
        if (now.isAfter(request.getPeriodEnd())) {
            long overdueMinutes = Duration.between(request.getPeriodEnd(), now).toMinutes();
            String description = String.format(
                    "超時還車 %d 分鐘（預計 %s，實際 %s）",
                    overdueMinutes, request.getPeriodEnd(), now);
            ViolationRecord v = new ViolationRecord(
                    UUID.randomUUID(),
                    request.getUserId(),
                    request.getVehicleId(),
                    request.getId(),
                    "OVERDUE",
                    description,
                    now);
            return Optional.of(violationRepo.save(v));
        }
        return Optional.empty();
    }

    /**
     * 取得所有違規記錄。
     *
     * @return 全部違規記錄清單
     */
    public List<ViolationRecord> listAll() {
        return violationRepo.findAll();
    }

    /**
     * 依使用者 ID 取得違規記錄。
     *
     * @param userId 使用者唯一識別碼
     * @return 該使用者的違規記錄清單
     */
    public List<ViolationRecord> listByUser(UUID userId) {
        return violationRepo.findByUserId(userId);
    }
}
