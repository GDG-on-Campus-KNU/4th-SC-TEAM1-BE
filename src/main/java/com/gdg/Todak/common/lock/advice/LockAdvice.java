package com.gdg.Todak.common.lock.advice;

import com.gdg.Todak.common.domain.ApiResponse;
import com.gdg.Todak.common.lock.exception.LockException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.gdg.Todak.common")
public class LockAdvice {

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(LockException.class)
    public ApiResponse<Object> handleLockException(LockException e) {
        return ApiResponse.of(
                HttpStatus.CONFLICT,
                e.getMessage()
        );
    }
}
