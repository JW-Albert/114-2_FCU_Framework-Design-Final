package com.vehicle.management.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登入失敗鎖定服務（記憶體實作，適用 prototype）。
 *
 * <p>同一帳號連續登入失敗達 {@value #MAX_ATTEMPTS} 次後，鎖定 {@value #LOCK_MINUTES} 分鐘。
 * 成功登入即清除計數。重啟後計數歸零（記憶體儲存）。</p>
 */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_MINUTES = 15;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(LOCK_MINUTES);

    private static final class Attempt {
        int count;
        Instant lockedUntil;
    }

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    /**
     * 若帳號目前處於鎖定狀態則拋出例外。
     *
     * @param email 帳號 Email
     * @throws AccountLockedException 若帳號仍在鎖定期間
     */
    public void assertNotLocked(String email) {
        Attempt a = attempts.get(key(email));
        if (a != null && a.lockedUntil != null && Instant.now().isBefore(a.lockedUntil)) {
            long mins = Math.max(1, Duration.between(Instant.now(), a.lockedUntil).toMinutes());
            throw new AccountLockedException(
                    "帳號因多次登入失敗已鎖定，請於約 " + mins + " 分鐘後再試");
        }
    }

    /** 記錄一次登入失敗，達上限即鎖定。 */
    public void recordFailure(String email) {
        Attempt a = attempts.computeIfAbsent(key(email), k -> new Attempt());
        a.count++;
        if (a.count >= MAX_ATTEMPTS) {
            a.lockedUntil = Instant.now().plus(LOCK_DURATION);
        }
    }

    /** 登入成功，清除該帳號的失敗計數與鎖定。 */
    public void recordSuccess(String email) {
        attempts.remove(key(email));
    }

    private String key(String email) {
        return email == null ? "" : email.toLowerCase();
    }
}
