package com.vehicle.management.domain.observer;

import com.vehicle.management.domain.model.BorrowingRequest;
import com.vehicle.management.domain.model.User;
import com.vehicle.management.repository.IUserRepository;
import com.vehicle.management.service.NotificationService;
import org.springframework.stereotype.Component;

/**
 * 收件夾通知觀察者（ConcreteObserver，Observer Pattern Ch20）。
 *
 * <p>將借車生命週期事件寫入相關使用者的站內收件夾。
 * 與 {@link EmailNotificationObserver} 並存，示範 Observer Pattern 的 OCP 擴展性——
 * 新增此通知管道<b>無需修改</b> {@link com.vehicle.management.service.BorrowingService}。</p>
 *
 * <p><b>收件人規則：</b>
 * <ul>
 *   <li>送出申請：通知可審核者（所有 ADMIN，以及與申請人同部門的 MANAGER）。</li>
 *   <li>核准 / 拒絕 / 出車 / 還車：通知申請人本人。</li>
 * </ul>
 */
@Component
public class InboxNotificationObserver implements BorrowingEventObserver {

    private final NotificationService notificationService;
    private final IUserRepository userRepo;

    public InboxNotificationObserver(NotificationService notificationService, IUserRepository userRepo) {
        this.notificationService = notificationService;
        this.userRepo = userRepo;
    }

    @Override
    public void onSubmitted(BorrowingRequest request) {
        String requesterDept = userRepo.findById(request.getUserId())
                .map(User::getDepartment).orElse(null);
        userRepo.findAll().stream()
                .filter(u -> canReview(u, requesterDept))
                .forEach(u -> notificationService.create(
                        u.getId(), "BORROWING_SUBMITTED", "新借車申請待審核",
                        "有一筆新的借車申請正在等待您審核。", request.getId()));
    }

    @Override
    public void onApproved(BorrowingRequest request) {
        notificationService.create(request.getUserId(), "BORROWING_APPROVED",
                "借車申請已核准", "您的借車申請已核准，請於指定時段出車。", request.getId());
    }

    @Override
    public void onRejected(BorrowingRequest request) {
        notificationService.create(request.getUserId(), "BORROWING_REJECTED",
                "借車申請已拒絕",
                "您的借車申請已被拒絕。原因：" + (request.getReviewNote() == null ? "未提供" : request.getReviewNote()),
                request.getId());
    }

    @Override
    public void onStarted(BorrowingRequest request) {
        notificationService.create(request.getUserId(), "BORROWING_STARTED",
                "車輛已出車", "您的借用車輛已出車，祝行車平安。", request.getId());
    }

    @Override
    public void onCompleted(BorrowingRequest request) {
        notificationService.create(request.getUserId(), "BORROWING_COMPLETED",
                "車輛已還車", "您的借用已完成還車，感謝使用。", request.getId());
    }

    /** 判斷使用者是否為此申請的可審核者（ADMIN，或同部門 MANAGER）。 */
    private boolean canReview(User u, String requesterDept) {
        boolean isAdmin = u.getRoles().stream().anyMatch(r -> "ADMIN".equals(r.getName()));
        boolean isSameDeptManager = requesterDept != null
                && requesterDept.equals(u.getDepartment())
                && u.getRoles().stream().anyMatch(r -> "MANAGER".equals(r.getName()));
        return isAdmin || isSameDeptManager;
    }
}
