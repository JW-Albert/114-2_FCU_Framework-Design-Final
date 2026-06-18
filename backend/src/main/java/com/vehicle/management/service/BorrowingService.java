package com.vehicle.management.service;

import com.vehicle.management.domain.chain.BorrowingValidationContext;
import com.vehicle.management.domain.chain.BorrowingValidator;
import com.vehicle.management.domain.model.BorrowingRequest;
import com.vehicle.management.domain.model.User;
import com.vehicle.management.domain.observer.BorrowingEventObserver;
import com.vehicle.management.domain.observer.BorrowingEventPublisher;
import com.vehicle.management.domain.role.Permission;
import com.vehicle.management.repository.IBorrowingRepository;
import com.vehicle.management.repository.IUserRepository;
import com.vehicle.management.repository.IVehicleRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 借車申請業務邏輯服務。
 *
 * <p><b>套用的設計原則與設計模式：</b>
 * <ul>
 *   <li><b>SRP</b>：只負責借車申請的生命週期管理（送出、審核、出車、還車）。</li>
 *   <li><b>Observer Pattern（Ch20）</b>：繼承 {@link BorrowingEventPublisher}，
 *       狀態改變後廣播通知，無需直接呼叫通知類別（DIP）。</li>
 *   <li><b>Strategy Pattern（Ch18）</b>：注入 {@link ConflictCheckStrategy}，
 *       衝突判斷演算法可在不修改此類別的情況下替換（OCP）。</li>
 *   <li><b>State Pattern（Ch13）</b>：透過 {@link BorrowingRequest#approve} 等方法委派給
 *       狀態物件，遵守迪米特法則（LoD），不直接操作狀態字串。</li>
 *   <li><b>DIP</b>：依賴 {@link IBorrowingRepository} 與 {@link IVehicleRepository} 介面。</li>
 * </ul>
 */
@Service
public class BorrowingService extends BorrowingEventPublisher {

    private final IBorrowingRepository borrowingRepo;
    private final IVehicleRepository vehicleRepo;
    private final IUserRepository userRepo;
    /** 違規記錄服務，於還車時偵測超時並建立違規記錄。 */
    private final ViolationService violationService;
    /** Chain of Responsibility：依序執行借車申請的各項驗證節點。 */
    private final List<BorrowingValidator> validators;

    /**
     * 建構借車服務。
     *
     * <p>Spring 自動注入所有實作 {@link BorrowingEventObserver} 的 Bean（Observer Pattern），
     * 以及所有 {@link BorrowingValidator} Bean（Chain of Responsibility Pattern）。</p>
     *
     * @param borrowingRepo    借車申請儲存庫
     * @param vehicleRepo      車輛儲存庫
     * @param userRepo         使用者儲存庫（用於 MANAGER 部門驗證）
     * @param violationService 違規記錄服務（@Lazy 避免循環依賴）
     * @param observers        所有已註冊的事件觀察者，由 Spring 自動收集
     * @param validators       借車申請驗證責任鏈節點，由 Spring 依 @Order 排序後注入
     */
    public BorrowingService(IBorrowingRepository borrowingRepo,
                            IVehicleRepository vehicleRepo,
                            IUserRepository userRepo,
                            @Lazy ViolationService violationService,
                            List<BorrowingEventObserver> observers,
                            List<BorrowingValidator> validators) {
        this.borrowingRepo = borrowingRepo;
        this.vehicleRepo = vehicleRepo;
        this.userRepo = userRepo;
        this.violationService = violationService;
        this.validators = validators;
        observers.forEach(this::addObserver);
    }

    /**
     * 送出借車申請。
     *
     * <p>Chain of Responsibility Pattern：將驗證邏輯委派給責任鏈
     * （{@link com.vehicle.management.domain.chain.PermissionValidator} →
     * {@link com.vehicle.management.domain.chain.VehicleExistenceValidator} →
     * {@link com.vehicle.management.domain.chain.TimeConflictValidator}），
     * 任一節點驗證失敗即丟出例外，本方法只負責最終建立並儲存申請。</p>
     *
     * @param actor     送出申請的使用者
     * @param vehicleId 欲借用的車輛 ID
     * @param start     借車開始時間
     * @param end       借車結束時間
     * @param purpose   借車事由
     * @return 已儲存的借車申請（狀態為 PENDING）
     */
    public BorrowingRequest submitRequest(User actor, UUID vehicleId,
                                          Instant start, Instant end, String purpose) {
        BorrowingValidationContext ctx = new BorrowingValidationContext(actor, vehicleId, start, end);
        validators.forEach(v -> v.validate(ctx));

        BorrowingRequest request = new BorrowingRequest(
                UUID.randomUUID(), actor.getId(), vehicleId, start, end, purpose, Instant.now());
        return borrowingRepo.save(request);
    }

    /**
     * 審核通過借車申請（PENDING → APPROVED）。
     *
     * @param actor     執行審核的管理員
     * @param requestId 申請單 ID
     * @param note      審核備註
     * @return 已更新的借車申請（狀態為 APPROVED）
     * @throws PermissionDeniedException             若使用者不具備審核權限
     * @throws ResourceNotFoundException             若申請單 ID 不存在
     * @throws com.vehicle.management.domain.state.InvalidStateTransitionException
     *         若申請目前狀態不允許核准
     */
    public BorrowingRequest approveRequest(User actor, UUID requestId, String note) {
        if (!actor.can(Permission.APPROVE_BORROWING)) {
            throw new PermissionDeniedException(actor.getEmail() + " cannot approve requests");
        }
        BorrowingRequest request = getRequest(requestId);
        // MANAGER 部門範圍限制：只能審核同部門申請者的申請
        checkManagerDepartmentScope(actor, request);
        request.approve(note);  // State Pattern：委派給 PendingState
        BorrowingRequest saved = borrowingRepo.save(request);
        notifyApproved(saved);  // Observer Pattern：廣播核准事件
        return saved;
    }

    /**
     * 拒絕借車申請（PENDING → REJECTED）。
     *
     * @param actor     執行審核的管理員
     * @param requestId 申請單 ID
     * @param note      拒絕原因
     * @return 已更新的借車申請（狀態為 REJECTED）
     * @throws PermissionDeniedException 若使用者不具備審核權限
     * @throws ResourceNotFoundException 若申請單 ID 不存在
     */
    public BorrowingRequest rejectRequest(User actor, UUID requestId, String note) {
        if (!actor.can(Permission.APPROVE_BORROWING)) {
            throw new PermissionDeniedException(actor.getEmail() + " cannot reject requests");
        }
        BorrowingRequest request = getRequest(requestId);
        // MANAGER 部門範圍限制：只能審核同部門申請者的申請
        checkManagerDepartmentScope(actor, request);
        request.reject(note);  // State Pattern：委派給 PendingState
        BorrowingRequest saved = borrowingRepo.save(request);
        notifyRejected(saved);  // Observer Pattern：廣播拒絕事件
        return saved;
    }

    /**
     * 執行出車（APPROVED → IN_USE），同步更新車輛狀態為 IN_USE。
     *
     * @param requestId 申請單 ID
     * @return 已更新的借車申請（狀態為 IN_USE）
     * @throws ResourceNotFoundException 若申請單 ID 不存在
     */
    public BorrowingRequest startUse(UUID requestId) {
        BorrowingRequest request = getRequest(requestId);
        // 同步更新車輛狀態為使用中
        vehicleRepo.findById(request.getVehicleId()).ifPresent(v -> {
            v.markInUse();
            vehicleRepo.save(v);
        });
        request.startUse();  // State Pattern：委派給 ApprovedState
        BorrowingRequest saved = borrowingRepo.save(request);
        notifyStarted(saved);  // Observer Pattern：廣播出車事件
        return saved;
    }

    /**
     * 執行還車（IN_USE → RETURNED），同步更新車輛狀態為 AVAILABLE。
     *
     * @param requestId 申請單 ID
     * @return 已更新的借車申請（狀態為 RETURNED）
     * @throws ResourceNotFoundException 若申請單 ID 不存在
     */
    public BorrowingRequest completeUse(UUID requestId, int endMileage) {
        BorrowingRequest request = getRequest(requestId);
        // 同步更新車輛狀態為可用，並更新累積里程
        vehicleRepo.findById(request.getVehicleId()).ifPresent(v -> {
            v.markAvailable();
            v.updateMileage(endMileage);
            vehicleRepo.save(v);
        });
        request.complete(endMileage);  // State Pattern：委派給 InUseState，記錄結束里程
        BorrowingRequest saved = borrowingRepo.save(request);
        notifyCompleted(saved);  // Observer Pattern：廣播還車事件
        // 自動偵測超時並建立違規記錄
        if (violationService != null) {
            violationService.checkAndCreateOverdue(saved);
        }
        return saved;
    }

    /**
     * 取得所有待審核申請。
     *
     * @return 狀態為 PENDING 的申請清單
     */
    public List<BorrowingRequest> listPending() {
        return borrowingRepo.findPending();
    }

    /**
     * 取得指定使用者的所有申請紀錄。
     *
     * @param userId 使用者唯一識別碼
     * @return 該使用者的申請清單
     */
    public List<BorrowingRequest> listMyRequests(UUID userId) {
        return borrowingRepo.findByUserId(userId);
    }

    /**
     * 取得系統所有借車申請（管理員用）。
     *
     * @return 全部借車申請清單
     */
    public List<BorrowingRequest> listAll() {
        return borrowingRepo.findAll();
    }

    /**
     * 查詢指定車輛與時段是否有衝突申請。
     */
    public List<BorrowingRequest> findConflicts(UUID vehicleId, Instant start, Instant end) {
        return borrowingRepo.findConflicting(vehicleId, start, end);
    }

    /**
     * 取得指定時段內的借車申請（月曆視圖用）。
     *
     * @param start 時段開始時間
     * @param end   時段結束時間
     * @return 時段內的申請清單（不含已拒絕）
     */
    public List<BorrowingRequest> listInRange(Instant start, Instant end) {
        return borrowingRepo.findInRange(start, end);
    }

    /**
     * 依 ID 取得借車申請，若不存在則拋出例外。
     *
     * @param id 申請單 ID
     * @return 借車申請物件
     * @throws ResourceNotFoundException 若申請單不存在
     */
    private BorrowingRequest getRequest(UUID id) {
        return borrowingRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + id));
    }

    /**
     * 若 actor 為 MANAGER 角色，驗證申請者與 actor 屬於同一部門。
     * ADMIN 角色不受此限制。
     *
     * @param actor   執行審核的使用者
     * @param request 借車申請
     * @throws PermissionDeniedException 若部門不符或申請者未設定部門
     */
    private void checkManagerDepartmentScope(User actor, BorrowingRequest request) {
        boolean isManager = actor.getRoles().stream()
                .anyMatch(r -> "MANAGER".equals(r.getName()));
        if (!isManager) return;
        if (userRepo == null) return;

        String managerDept = actor.getDepartment();
        if (managerDept == null || managerDept.isBlank()) {
            throw new PermissionDeniedException("Manager has no department assigned");
        }
        User requester = userRepo.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requester not found: " + request.getUserId()));
        if (!managerDept.equals(requester.getDepartment())) {
            throw new PermissionDeniedException(
                    "Manager can only review requests from the same department");
        }
    }
}
