package com.vehicle.management.domain.command;

import com.vehicle.management.domain.model.BorrowingRequest;
import com.vehicle.management.service.BorrowingService;

import java.util.UUID;

/** 出車命令（ConcreteCommand）。 */
public class StartUseCommand implements BorrowingCommand {

    private final BorrowingService service;
    private final UUID requestId;

    public StartUseCommand(BorrowingService service, UUID requestId) {
        this.service = service;
        this.requestId = requestId;
    }

    @Override
    public BorrowingRequest execute() {
        return service.startUse(requestId);
    }

    @Override
    public String describe() {
        return "StartUseCommand[requestId=" + requestId + "]";
    }
}
