package com.gdg.Todak.guestbook.controller.advice;

import com.gdg.Todak.common.domain.ApiResponse;
import com.gdg.Todak.guestbook.exception.NotFoundException;
import com.gdg.Todak.guestbook.exception.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.gdg.Todak.guestbook")
public class GuestbookControllerAdvice {

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorizedException.class)
    public ApiResponse<Exception> unauthorizedException(UnauthorizedException e) {
        return ApiResponse.of(
            HttpStatus.UNAUTHORIZED,
            e.getMessage()
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ApiResponse<Exception> notFoundException(NotFoundException e) {
        return ApiResponse.of(
            HttpStatus.NOT_FOUND,
            e.getMessage()
        );
    }
}
