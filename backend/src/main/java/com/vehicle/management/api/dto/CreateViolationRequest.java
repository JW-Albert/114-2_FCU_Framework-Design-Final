package com.vehicle.management.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * 手動登錄違規記錄請求 DTO。
 *
 * <p>違規一律關聯到一筆既有借用記錄；違規者與車輛由後端自該借用記錄帶出，
 * 因此此處只需提供借用記錄 ID、違規類型與描述。</p>
 */
public record CreateViolationRequest(
        @NotNull(message = "借用記錄 ID 不可為空")
        UUID borrowingId,

        @NotBlank(message = "違規類型不可為空")
        String type,

        @NotBlank(message = "違規描述不可為空")
        String description
) {}
