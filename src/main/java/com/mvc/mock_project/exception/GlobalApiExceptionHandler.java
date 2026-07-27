package com.mvc.mock_project.exception;

import com.mvc.mock_project.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import lombok.RequiredArgsConstructor;
import java.util.Locale;

@RestControllerAdvice(annotations = RestController.class)
@RequiredArgsConstructor
public class GlobalApiExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiResponse<Void> response = ApiResponse.error("Validation failed");
        response.setErrors(errors);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String msg = ex.getMessage();
        if (ex.getCause() != null) {
            msg += " | Cause: " + ex.getCause().getMessage();
            if (ex.getCause().getCause() != null) {
                msg += " | Root: " + ex.getCause().getCause().getMessage();
            }
        }
        String message = "A database error occurred: " + msg;
        return ResponseEntity.badRequest().body(ApiResponse.error(message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        String errorMessage = ex.getMessage();
        try {
            Locale locale = LocaleContextHolder.getLocale();
            errorMessage = messageSource.getMessage(errorMessage, null, locale);
        } catch (NoSuchMessageException e) {
            // keep original message if no translation is found
        }
        return ResponseEntity.internalServerError().body(ApiResponse.error(errorMessage));
    }

    @ExceptionHandler(BatchValidationException.class)
    public ResponseEntity<Map<String, Object>> handleBatchValidationException(BatchValidationException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", ex.getMessage());
        response.put("data", ex.getErrors()); // The array of errors with row indices
        return ResponseEntity.badRequest().body(response);
    }
    @ExceptionHandler({
            DuplicateCodeException.class, 
            ResourceInUseException.class, 
            CommissionTierOverlapException.class, 
            InvalidTierStatusTransitionException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBusinessExceptions(RuntimeException ex) {
        String errorMessage = ex.getMessage();
        try {
            Locale locale = LocaleContextHolder.getLocale();
            errorMessage = messageSource.getMessage(errorMessage, null, locale);
        } catch (NoSuchMessageException e) {
            // keep original message if no translation is found
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(errorMessage));
    }
}
