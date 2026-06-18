package com.vehicle.management.domain.command;

import com.vehicle.management.domain.model.BorrowingRequest;

/**
 * 借車申請命令介面（Command Pattern, GoF Ch12）。
 *
 * <p>將「操作的請求」封裝為物件，使呼叫端（Controller）與業務邏輯（Service）解耦。
 * {@link com.vehicle.management.service.BorrowingCommandBus} 作為 Invoker，
 * 集中處理稽核記錄等橫切關注點，無需修改各個 Service 方法（OCP）。</p>
 */
public interface BorrowingCommand {
    /** 執行命令並回傳受影響的借車申請。 */
    BorrowingRequest execute();

    /** 回傳可讀的命令描述，用於稽核日誌。 */
    String describe();
}
