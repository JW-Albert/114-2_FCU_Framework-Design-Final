package com.vehicle.management.service;

import com.vehicle.management.domain.command.BorrowingCommand;
import com.vehicle.management.domain.model.BorrowingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 借車命令匯流排（Invoker，Command Pattern, GoF Ch12）。
 *
 * <p>作為統一的命令執行入口，集中處理所有狀態變更操作的稽核記錄。
 * 未來可在此加入命令排隊、回滾、非同步執行等能力，
 * 而無需修改各個 {@link BorrowingCommand} 實作（OCP）。</p>
 */
@Service
public class BorrowingCommandBus {

    private static final Logger log = LoggerFactory.getLogger(BorrowingCommandBus.class);

    /**
     * 執行借車命令並記錄稽核日誌。
     *
     * @param command 要執行的命令
     * @return 執行後的借車申請物件
     */
    public BorrowingRequest dispatch(BorrowingCommand command) {
        BorrowingRequest result = command.execute();
        log.info("[AUDIT] {} → id={}", command.describe(), result.getId());
        return result;
    }
}
