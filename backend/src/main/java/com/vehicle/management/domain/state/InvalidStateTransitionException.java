package com.vehicle.management.domain.state;

public class InvalidStateTransitionException extends RuntimeException {
    public InvalidStateTransitionException(String currentState, String operation) {
        super("Cannot perform '" + operation + "' on a request in state '" + currentState + "'");
    }
}
