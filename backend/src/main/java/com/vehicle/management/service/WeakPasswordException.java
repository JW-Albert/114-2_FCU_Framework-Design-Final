package com.vehicle.management.service;

/** 密碼不符強度政策時拋出（由 GlobalExceptionHandler 映射為 400）。 */
public class WeakPasswordException extends RuntimeException {
    public WeakPasswordException(String message) {
        super(message);
    }
}
