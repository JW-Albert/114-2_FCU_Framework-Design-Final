package com.vehicle.management.domain.command;

import com.vehicle.management.domain.model.BorrowingRequest;
import com.vehicle.management.domain.model.User;
import com.vehicle.management.service.BorrowingService;

import java.util.UUID;

/** 撤銷核准命令（ConcreteCommand）：APPROVED → PENDING。 */
public class RevokeCommand implements BorrowingCommand {

    private final BorrowingService service;
    private final User actor;
    private final UUID requestId;
    private final String note;

    public RevokeCommand(BorrowingService service, User actor, UUID requestId, String note) {
        this.service = service;
        this.actor = actor;
        this.requestId = requestId;
        this.note = note;
    }

    @Override
    public BorrowingRequest execute() {
        return service.revokeApproval(actor, requestId, note);
    }

    @Override
    public String describe() {
        return "RevokeCommand[requestId=" + requestId + ", actor=" + actor.getEmail() + "]";
    }
}
