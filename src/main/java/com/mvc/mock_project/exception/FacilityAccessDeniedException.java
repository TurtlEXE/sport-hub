package com.mvc.mock_project.exception;

public class FacilityAccessDeniedException extends RuntimeException {
    public FacilityAccessDeniedException(String message) {
        super(message);
    }
}
