package com.gdg.Todak.diary.controller.advice;

import com.gdg.Todak.common.domain.ApiResponse;
import com.gdg.Todak.diary.exception.BadRequestException;
import com.gdg.Todak.diary.exception.ConflictException;
import com.gdg.Todak.diary.exception.NotFoundException;
import com.gdg.Todak.diary.exception.UnauthorizedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.gdg.Todak.diary")
public class DiaryControllerAdvice {

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorizedException.class)
    public ApiResponse<Exception> handleUnauthorizedException(UnauthorizedException e) {
        return ApiResponse.of(
                HttpStatus.UNAUTHORIZED,
                e.getMessage()
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ApiResponse<Exception> handleNotFoundException(NotFoundException e) {
        return ApiResponse.of(
                HttpStatus.NOT_FOUND,
                e.getMessage()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    public ApiResponse<Object> handleBadRequestException(BadRequestException e) {
        return ApiResponse.of(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
        );
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiResponse<Object> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        return ApiResponse.of(
                HttpStatus.CONFLICT,
                e.getMostSpecificCause().getMessage()
        );
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ConflictException.class)
    public ApiResponse<Exception> handleConflictException(ConflictException e) {
        return ApiResponse.of(
                HttpStatus.CONFLICT,
                e.getMessage()
        );
    }
}
