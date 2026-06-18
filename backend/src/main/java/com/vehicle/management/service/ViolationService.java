package com.vehicle.management.service;

import com.vehicle.management.domain.model.BorrowingRequest;
import com.vehicle.management.domain.model.ViolationRecord;
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
 * <p>負責偵測超時還車事件並自動建立違規記錄。
 * 由 {@link BorrowingService#completeUse} 在還車時呼叫。</p>
 */
@Service
public class ViolationService {

    private final IViolationRepository violationRepo;

    public ViolationService(IViolationRepository violationRepo) {
        this.violationRepo = violationRepo;
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
