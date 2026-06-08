package com.vehicle.management.domain.model;

/**
 * 車輛狀態列舉。
 *
 * <p>合法狀態轉換由 {@link Vehicle} 的方法負責守衛，
 * 非法轉換將拋出 {@link IllegalStateException}。
 *
 * <ul>
 *   <li>{@code AVAILABLE}  → {@code IN_USE}      : {@link Vehicle#markInUse()}</li>
 *   <li>{@code IN_USE}     → {@code AVAILABLE}   : {@link Vehicle#markAvailable()}</li>
 *   <li>{@code AVAILABLE}  → {@code MAINTENANCE} : {@link Vehicle#markMaintenance()}</li>
 *   <li>{@code MAINTENANCE}→ {@code AVAILABLE}   : {@link Vehicle#markAvailable()}</li>
 * </ul>
 */
public enum VehicleStatus {
    /** 車輛閒置，可接受新借車申請。 */
    AVAILABLE,
    /** 車輛正在使用中，無法接受新借車申請。 */
    IN_USE,
    /** 車輛正在維修保養，暫不可借用。 */
    MAINTENANCE
}
