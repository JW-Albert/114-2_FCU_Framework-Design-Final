package com.vehicle.management.domain.command;

import com.vehicle.management.domain.model.BorrowingRequest;
import com.vehicle.management.domain.model.User;
import com.vehicle.management.service.BorrowingService;

import java.util.UUID;

/** 核准借車申請命令（ConcreteCommand）。 */
public class ApproveCommand implements BorrowingCommand {

    private final BorrowingService service;
    private final User actor;
    private final UUID requestId;
    private final String note;

    public ApproveCommand(BorrowingService service, User actor, UUID requestId, String note) {
        this.service = service;
        this.actor = actor;
        this.requestId = requestId;
        this.note = note;
    }

    @Override
    public BorrowingRequest execute() {
        return service.approveRequest(actor, requestId, note);
    }

    @Override
    public String describe() {
        return "ApproveCommand[requestId=" + requestId + ", actor=" + actor.getEmail() + "]";
    }
}
