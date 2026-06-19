package com.vehicle.management.service;

/** 帳號因多次登入失敗遭暫時鎖定時拋出（由 GlobalExceptionHandler 映射為 423 Locked）。 */
public class AccountLockedException extends RuntimeException {
    public AccountLockedException(String message) {
        super(message);
    }
}
