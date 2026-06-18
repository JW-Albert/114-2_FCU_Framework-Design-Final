package com.vehicle.management.domain.role;

/**
 * 系統操作權限列舉。
 *
 * <p>每個 {@link Role} 實作持有一組 {@code Permission}，
 * {@link com.vehicle.management.domain.model.User#can(Permission)} 透過此列舉進行授權判斷。
 * 新增功能只需在此列舉加入新常數，無需修改 {@code User} 類別（OCP）。</p>
 */
public enum Permission {

    /** 核准或拒絕借車申請，以及執行出車與還車動作。 */
    APPROVE_BORROWING,

    /** 新增、修改、刪除車輛資料與保養紀錄。 */
    MANAGE_VEHICLE,

    /** 新增、修改、刪除使用者帳號及變更角色。 */
    MANAGE_USER,

    /** 手動登錄違規記錄（管理員與部門主管適用）。 */
    MANAGE_VIOLATION,

    /** 送出借車申請。 */
    SUBMIT_REQUEST
}
