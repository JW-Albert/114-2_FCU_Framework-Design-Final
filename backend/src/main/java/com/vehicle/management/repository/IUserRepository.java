package com.vehicle.management.repository;

import com.vehicle.management.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 使用者儲存庫介面（Repository Interface）。
 *
 * <p><b>依賴反轉原則（DIP）：</b>Service 層依賴此介面，不依賴具體的 JPA 或 InMemory 實作，
 * 使業務邏輯可在不同儲存機制（PostgreSQL、H2、InMemory）之間自由切換。</p>
 *
 * <p>提供兩種實作：
 * <ul>
 *   <li>{@code UserRepositoryAdapter} — 生產環境使用，委派給 Spring Data JPA（Adapter Pattern）</li>
 *   <li>{@code InMemoryUserRepository} — 單元測試用，不依賴資料庫</li>
 * </ul>
 */
public interface IUserRepository {

    /**
     * 依 ID 查詢使用者。
     *
     * @param id 使用者唯一識別碼
     * @return 包含使用者的 {@link Optional}，若不存在則為空
     */
    Optional<User> findById(UUID id);

    /**
     * 依 Email 查詢使用者。
     *
     * @param email 使用者 Email（登入帳號）
     * @return 包含使用者的 {@link Optional}，若不存在則為空
     */
    Optional<User> findByEmail(String email);

    /**
     * 檢查 Email 是否已被使用。
     *
     * @param email 欲檢查的 Email
     * @return 若已存在同 Email 的帳號回傳 {@code true}
     */
    boolean existsByEmail(String email);

    /**
     * 儲存使用者（新增或更新）。
     *
     * @param user 欲儲存的使用者物件
     * @return 儲存後的使用者物件（可能含有資料庫產生的欄位）
     */
    User save(User user);

    /**
     * 查詢所有使用者。
     *
     * @return 系統中所有使用者清單
     */
    List<User> findAll();

    /**
     * 依 ID 刪除使用者。
     *
     * @param id 欲刪除的使用者唯一識別碼
     */
    void deleteById(UUID id);
}
