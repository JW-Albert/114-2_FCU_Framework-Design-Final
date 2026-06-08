package com.vehicle.management.domain.model;

import com.vehicle.management.domain.role.Permission;
import com.vehicle.management.domain.role.Role;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 使用者領域物件（Domain Entity）。
 *
 * <p><b>OOP 設計重點：</b>
 * <ul>
 *   <li><b>不可變物件（Immutable Value Object 風格）</b>：除 {@code roles} 外所有欄位皆為 {@code final}，
 *       修改操作（更名、換角色）由 Service 層建立新物件替換，避免意外的狀態突變。</li>
 *   <li><b>封裝（Encapsulation）</b>：角色集合透過 {@link #can(Permission)} 公開能力查詢，
 *       Service 層不應直接存取 {@code roles}，從而遵守最少知識原則（LoD）。</li>
 *   <li><b>組合優於繼承（Composition over Inheritance）</b>：以 {@link Set}{@code <}{@link Role}{@code >}
 *       組合角色，比繼承更彈性地支援多角色擴充。</li>
 * </ul>
 */
public class User {

    private final UUID id;
    private final String name;
    private final String email;
    private final String passwordHash;
    private final Set<Role> roles;
    private final Instant createdAt;

    /**
     * 建構使用者物件。
     *
     * @param id           使用者唯一識別碼
     * @param name         顯示名稱
     * @param email        登入用 Email（系統內唯一）
     * @param passwordHash BCrypt 雜湊後的密碼，不應傳入明文
     * @param roles        使用者擁有的角色集合（至少一個）
     * @param createdAt    帳號建立時間
     */
    public User(UUID id, String name, String email, String passwordHash,
                Set<Role> roles, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roles = new HashSet<>(roles);
        this.createdAt = createdAt;
    }

    /**
     * 檢查此使用者是否擁有指定的操作權限。
     *
     * <p>透過多型遍歷所有角色的 {@link Role#getPermissions()}，
     * 實現「一次呼叫、不感知具體角色」的封裝。
     * Service 層<b>必須</b>透過此方法進行授權檢查，不得直接存取 {@code roles}。
     *
     * @param permission 欲檢查的權限
     * @return 若使用者透過任一角色持有該權限則回傳 {@code true}
     */
    public boolean can(Permission permission) {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(p -> p == permission);
    }

    /** @return 使用者唯一識別碼 */
    public UUID getId() { return id; }

    /** @return 使用者顯示名稱 */
    public String getName() { return name; }

    /** @return 使用者 Email（登入帳號） */
    public String getEmail() { return email; }

    /** @return BCrypt 雜湊後的密碼字串 */
    public String getPasswordHash() { return passwordHash; }

    /**
     * @return 使用者角色的不可變副本，避免外部直接修改內部集合
     */
    public Set<Role> getRoles() { return Set.copyOf(roles); }

    /** @return 帳號建立時間 */
    public Instant getCreatedAt() { return createdAt; }
}
