package com.example.elderly.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        return ApiResponse.error(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().isEmpty()
                ? "参数错误"
                : ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ApiResponse.error(400, message);
    }

    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleBindException(BindException ex) {
        String message = ex.getAllErrors().isEmpty()
                ? "参数错误"
                : ex.getAllErrors().get(0).getDefaultMessage();
        return ApiResponse.error(400, message);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception ex) {
        logger.error("Unhandled exception", ex);
        String message = ex.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = "服务器内部错误";
        }
        return ApiResponse.error(500, message);
    }
}
