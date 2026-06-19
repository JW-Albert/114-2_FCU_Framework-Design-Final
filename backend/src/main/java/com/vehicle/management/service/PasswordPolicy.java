package com.vehicle.management.service;

/**
 * 密碼強度政策。
 *
 * <p>要求：長度至少 8 個字元，且同時包含大寫字母、小寫字母與數字。
 * 於建立帳號（註冊 / 管理員建立）時驗證。</p>
 */
public final class PasswordPolicy {

    private static final int MIN_LENGTH = 8;

    private PasswordPolicy() {}

    /**
     * 驗證密碼是否符合強度政策。
     *
     * @param password 明文密碼
     * @throws WeakPasswordException 若不符合任一規則
     */
    public static void validate(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            throw new WeakPasswordException("密碼長度至少需 " + MIN_LENGTH + " 個字元");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new WeakPasswordException("密碼需包含至少一個大寫字母");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new WeakPasswordException("密碼需包含至少一個小寫字母");
        }
        if (!password.matches(".*\\d.*")) {
            throw new WeakPasswordException("密碼需包含至少一個數字");
        }
    }
}
