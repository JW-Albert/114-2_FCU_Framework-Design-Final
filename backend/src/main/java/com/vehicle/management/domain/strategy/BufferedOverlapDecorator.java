package com.vehicle.management.domain.strategy;

import com.vehicle.management.domain.model.BorrowingRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * 帶緩衝時段的衝突檢查裝飾器（Decorator Pattern, GoF Ch9）。
 *
 * <p>包裝任意 {@link ConflictCheckStrategy}，在檢查衝突前先將新申請的時段前後各延伸 {@code buffer}
 * 長度，確保借車申請之間保有最短準備時間（例如清潔、確認）。</p>
 *
 * <p>以 Decorator 模式實作，而非修改 {@link StrictOverlapStrategy}，符合開放封閉原則（OCP）：
 * 不影響其他使用相同介面的元件，可動態疊加多層裝飾。</p>
 *
 * <p><b>啟用方式（Spring）：</b>在設定類別中宣告 {@code @Bean @Primary}：
 * <pre>{@code
 * @Bean @Primary
 * ConflictCheckStrategy conflictCheckStrategy(StrictOverlapStrategy base) {
 *     return new BufferedOverlapDecorator(base, Duration.ofMinutes(30));
 * }
 * }</pre>
 * </p>
 */
public class BufferedOverlapDecorator implements ConflictCheckStrategy {

    private final ConflictCheckStrategy inner;
    private final Duration buffer;

    /**
     * @param inner  被裝飾的衝突檢查策略
     * @param buffer 在申請時段前後各加入的緩衝時間
     */
    public BufferedOverlapDecorator(ConflictCheckStrategy inner, Duration buffer) {
        this.inner = inner;
        this.buffer = buffer;
    }

    /**
     * 將新申請的時段前後各延伸 {@code buffer}，再委派給內層策略檢查衝突。
     */
    @Override
    public boolean hasConflict(List<BorrowingRequest> existingRequests,
                               Instant periodStart,
                               Instant periodEnd) {
        return inner.hasConflict(existingRequests,
                periodStart.minus(buffer),
                periodEnd.plus(buffer));
    }
}
