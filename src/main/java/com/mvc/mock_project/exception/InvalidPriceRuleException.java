package com.mvc.mock_project.exception;

public class InvalidPriceRuleException extends RuntimeException {
    public InvalidPriceRuleException(String message) {
        super(message);
    }
}
