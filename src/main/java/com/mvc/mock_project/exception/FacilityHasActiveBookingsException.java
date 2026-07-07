package com.mvc.mock_project.exception;

public class FacilityHasActiveBookingsException extends RuntimeException {
    public FacilityHasActiveBookingsException(String message) {
        super(message);
    }
}
