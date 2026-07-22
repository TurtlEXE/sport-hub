package com.mvc.mock_project.exception;

import lombok.Getter;
import java.util.List;
import java.util.Map;

@Getter
public class BatchValidationException extends RuntimeException {
    private final List<Map<String, Object>> errors;

    public BatchValidationException(String message, List<Map<String, Object>> errors) {
        super(message);
        this.errors = errors;
    }
}
