package com.vehicle.management.service;

import com.vehicle.management.domain.model.User;
import com.vehicle.management.domain.role.Permission;
import com.vehicle.management.domain.role.Role;
import com.vehicle.management.domain.role.RoleFactory;
import com.vehicle.management.repository.IUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 使用者帳號管理業務邏輯服務。
 *
 * <p><b>套用的設計原則：</b>
 * <ul>
 *   <li><b>SRP</b>：負責帳號的 CRUD（建立、查詢、更新、刪除）及密碼驗證，
 *       不處理 JWT 認證流程。</li>
 *   <li><b>DIP</b>：依賴 {@link IUserRepository} 介面，不感知具體 DB 實作。</li>
 *   <li><b>Factory Method（Ch11）</b>：透過 {@link RoleFactory#create} 建立角色物件，
 *       不直接 {@code new AdminRole()} 或 {@code new EmployeeRole()}。</li>
 * </ul>
 *
 * <p>管理員專屬操作（{@link #listAllUsers}、{@link #updateUser} 等）在執行前
 * 統一由 {@link #requireManageUser} 驗證 {@link Permission#MANAGE_USER} 權限。</p>
 */
@Service
public class UserService {

    private final IUserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    /**
     * @param userRepo        使用者儲存庫（由 Spring DI 注入）
     * @param passwordEncoder BCrypt 密碼編碼器（由 Spring DI 注入）
     */
    public UserService(IUserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 使用者自行註冊帳號（公開端點使用）。
     *
     * @param name        使用者名稱
     * @param email       登入 Email
     * @param rawPassword 明文密碼（將以 BCrypt 雜湊後儲存）
     * @param roleName    角色名稱（{@code "ADMIN"} 或 {@code "EMPLOYEE"}）
     * @return 已建立的使用者
     * @throws ConflictException 若 Email 已被註冊
     */
    public User register(String name, String email, String rawPassword, String roleName) {
        if (userRepo.existsByEmail(email)) {
            throw new ConflictException("Email already registered: " + email);
        }
        PasswordPolicy.validate(rawPassword);
        // Factory Method：透過工廠建立角色，不直接依賴具體角色類別
        Role role = RoleFactory.create(roleName);
        User user = new User(UUID.randomUUID(), name, email,
                passwordEncoder.encode(rawPassword), Set.of(role), Instant.now());
        return userRepo.save(user);
    }

    /**
     * 依 Email 查詢使用者（Spring Security 認證流程使用）。
     *
     * @param email 使用者 Email
     * @return 使用者物件
     * @throws ResourceNotFoundException 若 Email 不存在
     */
    public User findByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    /**
     * 依 ID 查詢使用者。
     *
     * @param id 使用者唯一識別碼
     * @return 使用者物件
     * @throws ResourceNotFoundException 若 ID 不存在
     */
    public User findById(UUID id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    /**
     * 驗證使用者密碼（登入時使用）。
     *
     * @param user        使用者物件
     * @param rawPassword 欲驗證的明文密碼
     * @return 密碼正確回傳 {@code true}
     */
    public boolean verifyPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }

    /**
     * 管理員查詢所有使用者帳號。
     *
     * @param caller 執行此操作的使用者（必須為管理員）
     * @return 系統所有使用者清單
     * @throws PermissionDeniedException 若呼叫者不具備 {@link Permission#MANAGE_USER}
     */
    public List<User> listAllUsers(User caller) {
        requireManageUser(caller);
        return userRepo.findAll();
    }

    /**
     * 管理員依 ID 查詢指定使用者。
     *
     * @param caller   執行此操作的使用者（必須為管理員）
     * @param targetId 欲查詢的使用者 ID
     * @return 使用者物件
     * @throws PermissionDeniedException 若呼叫者不具備管理用戶的權限
     * @throws ResourceNotFoundException 若目標使用者不存在
     */
    public User getUserById(User caller, UUID targetId) {
        requireManageUser(caller);
        return findById(targetId);
    }

    /**
     * 管理員建立新使用者帳號（可指定任意角色）。
     *
     * @param caller      執行此操作的管理員
     * @param name        新帳號名稱
     * @param email       新帳號 Email
     * @param rawPassword 新帳號明文密碼
     * @param roleName    新帳號角色
     * @return 已建立的使用者
     * @throws PermissionDeniedException 若呼叫者不具備管理用戶的權限
     * @throws ConflictException         若 Email 已被使用
     */
    public User createUserByAdmin(User caller, String name, String email,
                                  String rawPassword, String roleName) {
        return createUserByAdmin(caller, name, email, rawPassword, roleName, null);
    }

    /**
     * 管理員建立新使用者帳號（可指定角色與部門）。
     *
     * @param caller      執行此操作的管理員
     * @param name        新帳號名稱
     * @param email       新帳號 Email
     * @param rawPassword 新帳號明文密碼
     * @param roleName    新帳號角色
     * @param department  所屬部門（可為 null）
     * @return 已建立的使用者
     */
    public User createUserByAdmin(User caller, String name, String email,
                                  String rawPassword, String roleName, String department) {
        requireManageUser(caller);
        if (userRepo.existsByEmail(email)) {
            throw new ConflictException("Email already registered: " + email);
        }
        PasswordPolicy.validate(rawPassword);
        Role role = RoleFactory.create(roleName);
        User user = new User(UUID.randomUUID(), name, email,
                passwordEncoder.encode(rawPassword), Set.of(role), Instant.now(), department);
        return userRepo.save(user);
    }

    /**
     * 管理員更新使用者基本資料（名稱與 Email）。
     * 採用不可變物件替換策略，保留原有密碼、角色與建立時間。
     *
     * @param caller   執行此操作的管理員
     * @param targetId 欲更新的使用者 ID
     * @param name     新名稱
     * @param email    新 Email
     * @return 已更新的使用者物件
     * @throws PermissionDeniedException 若呼叫者不具備管理用戶的權限
     * @throws ResourceNotFoundException 若目標使用者不存在
     * @throws ConflictException         若新 Email 已被其他帳號使用
     */
    public User updateUser(User caller, UUID targetId, String name, String email) {
        return updateUser(caller, targetId, name, email, null);
    }

    /**
     * 管理員更新使用者基本資料（名稱、Email 與部門）。
     * 採用不可變物件替換策略，保留原有密碼、角色與建立時間。
     *
     * @param caller     執行此操作的管理員
     * @param targetId   欲更新的使用者 ID
     * @param name       新名稱
     * @param email      新 Email
     * @param department 新部門（null 表示保留原部門）
     * @return 已更新的使用者物件
     */
    public User updateUser(User caller, UUID targetId, String name, String email, String department) {
        requireManageUser(caller);
        User target = findById(targetId);
        // 若 Email 有變更，確認新 Email 未被其他帳號使用
        if (!target.getEmail().equalsIgnoreCase(email) && userRepo.existsByEmail(email)) {
            throw new ConflictException("Email already registered: " + email);
        }
        String effectiveDept = department != null ? department : target.getDepartment();
        // 不可變替換：建立包含新欄位的 User，保留密碼、角色與建立時間
        User updated = new User(target.getId(), name, email,
                target.getPasswordHash(), target.getRoles(), target.getCreatedAt(), effectiveDept);
        return userRepo.save(updated);
    }

    /**
     * 管理員變更使用者角色。
     *
     * @param caller    執行此操作的管理員
     * @param targetId  欲更新的使用者 ID
     * @param roleName  新角色名稱
     * @return 已更新的使用者物件
     * @throws PermissionDeniedException 若呼叫者不具備管理用戶的權限
     * @throws ResourceNotFoundException 若目標使用者不存在
     * @throws IllegalArgumentException  若 roleName 不合法
     */
    public User changeUserRole(User caller, UUID targetId, String roleName) {
        requireManageUser(caller);
        User target = findById(targetId);
        Role newRole = RoleFactory.create(roleName);  // Factory Method：統一建立角色
        User updated = new User(target.getId(), target.getName(), target.getEmail(),
                target.getPasswordHash(), Set.of(newRole), target.getCreatedAt());
        return userRepo.save(updated);
    }

    /**
     * 管理員刪除使用者帳號。
     *
     * @param caller   執行此操作的管理員
     * @param targetId 欲刪除的使用者 ID
     * @throws PermissionDeniedException 若呼叫者不具備管理用戶的權限
     * @throws ResourceNotFoundException 若目標使用者不存在
     */
    public void deleteUser(User caller, UUID targetId) {
        requireManageUser(caller);
        findById(targetId);  // 確認使用者存在，不存在則拋出 ResourceNotFoundException
        userRepo.deleteById(targetId);
    }

    /**
     * 驗證呼叫者擁有 {@link Permission#MANAGE_USER} 權限，否則拋出例外。
     *
     * @param caller 執行操作的使用者
     * @throws PermissionDeniedException 若呼叫者不具備管理用戶的權限
     */
    private void requireManageUser(User caller) {
        if (!caller.can(Permission.MANAGE_USER)) {
            throw new PermissionDeniedException("Only admins can manage user accounts");
        }
    }
}
