package com.mvc.mock_project.exception;

public class StaffAccessDeniedException extends RuntimeException {
    public StaffAccessDeniedException(String message) {
        super(message);
    }
}
