package com.dev.hotelbooking.exception;

import com.dev.hotelbooking.dto.response.ApiErrorResponse;
import com.dev.hotelbooking.enums.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(RuntimeException e) {
        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(errorCode.getHttpStatus(),
                errorCode.getMessage());

        return ResponseEntity.status(errorCode.getHttpStatus()).body(apiErrorResponse);
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiErrorResponse> handleAppException(AppException e) {
        ErrorCode errorCode = e.getErrorCode();
        String message = e.getCustomMessage() == null ? errorCode.getMessage() : e.getCustomMessage();

        ApiErrorResponse apiErrorResponse =
                new ApiErrorResponse(errorCode.getHttpStatus(), message);

        return ResponseEntity.status(errorCode.getHttpStatus()).body(apiErrorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
        Map<String, String> errors = new HashMap<>();

        for (ObjectError error : allErrors) {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        }

        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(errorCode.getHttpStatus(),
                errorCode.getMessage(), errors);

        return ResponseEntity.status(errorCode.getHttpStatus()).body(apiErrorResponse);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(errorCode.getHttpStatus(),
                errorCode.getMessage());
        return ResponseEntity.status(errorCode.getHttpStatus()).body(apiErrorResponse);
    }
}
