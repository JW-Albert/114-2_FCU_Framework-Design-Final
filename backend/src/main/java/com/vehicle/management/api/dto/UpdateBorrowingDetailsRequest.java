package com.vehicle.management.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/** 更改借車申請內容（時段與事由）請求 DTO。 */
public record UpdateBorrowingDetailsRequest(
        @NotNull(message = "開始時間不可為空")
        Instant periodStart,

        @NotNull(message = "結束時間不可為空")
        Instant periodEnd,

        String purpose
) {}
