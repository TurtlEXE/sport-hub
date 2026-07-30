package com.mvc.mock_project.exception;

public class InvalidTierStatusTransitionException extends RuntimeException {
    public InvalidTierStatusTransitionException(String message) {
        super(message);
    }
}
