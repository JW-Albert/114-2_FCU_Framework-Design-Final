package com.vehicle.management.api.dto;

import jakarta.validation.constraints.Min;

/** 還車請求 DTO，攜帶結束里程數。 */
public record CompleteBorrowingRequest(
        @Min(value = 0, message = "里程數不可為負數")
        int endMileage
) {}
