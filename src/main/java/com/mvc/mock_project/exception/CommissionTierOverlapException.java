package com.mvc.mock_project.exception;

public class CommissionTierOverlapException extends RuntimeException {
    public CommissionTierOverlapException(String message) {
        super(message);
    }
}
