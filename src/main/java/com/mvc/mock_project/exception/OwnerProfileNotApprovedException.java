package com.mvc.mock_project.exception;

public class OwnerProfileNotApprovedException extends RuntimeException {
    public OwnerProfileNotApprovedException(String message) {
        super(message);
    }
}
