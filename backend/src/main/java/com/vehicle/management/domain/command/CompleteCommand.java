package com.vehicle.management.domain.command;

import com.vehicle.management.domain.model.BorrowingRequest;
import com.vehicle.management.service.BorrowingService;

import java.util.UUID;

/** 還車命令（ConcreteCommand）。 */
public class CompleteCommand implements BorrowingCommand {

    private final BorrowingService service;
    private final UUID requestId;
    private final int endMileage;

    public CompleteCommand(BorrowingService service, UUID requestId, int endMileage) {
        this.service = service;
        this.requestId = requestId;
        this.endMileage = endMileage;
    }

    @Override
    public BorrowingRequest execute() {
        return service.completeUse(requestId, endMileage);
    }

    @Override
    public String describe() {
        return "CompleteCommand[requestId=" + requestId + ", endMileage=" + endMileage + "]";
    }
}
