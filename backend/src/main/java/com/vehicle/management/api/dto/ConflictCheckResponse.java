package com.vehicle.management.api.dto;

import java.util.List;

public record ConflictCheckResponse(boolean hasConflict, List<BorrowingResponse> conflicts) {}
