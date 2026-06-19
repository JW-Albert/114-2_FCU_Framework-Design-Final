package com.vehicle.management.repository;

import com.vehicle.management.domain.model.AuditLog;

import java.util.List;

/** 稽核日誌儲存庫介面（DIP）。 */
public interface IAuditRepository {

    /** 儲存一筆稽核日誌。 */
    AuditLog save(AuditLog log);

    /** 取得所有稽核日誌，依時間由新到舊排序。 */
    List<AuditLog> findAllRecent();
}
