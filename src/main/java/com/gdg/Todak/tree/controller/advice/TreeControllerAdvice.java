package com.gdg.Todak.tree.controller.advice;

import com.gdg.Todak.common.domain.ApiResponse;
import com.gdg.Todak.tree.exception.BadRequestException;
import com.gdg.Todak.tree.exception.NotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.gdg.Todak.tree")
public class TreeControllerAdvice {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ApiResponse<Exception> handleNotFoundException(NotFoundException e) {
        return ApiResponse.of(
                HttpStatus.NOT_FOUND,
                e.getMessage()
        );
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(com.gdg.Todak.point.exception.NotFoundException.class)
    public ApiResponse<Exception> handleNotFoundException(com.gdg.Todak.point.exception.NotFoundException e) {
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

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(com.gdg.Todak.point.exception.BadRequestException.class)
    public ApiResponse<Object> handleBadRequestException(com.gdg.Todak.point.exception.BadRequestException e) {
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

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public ApiResponse<Object> bindException(BindException e) {
        return ApiResponse.of(
                HttpStatus.BAD_REQUEST,
                e.getBindingResult().getAllErrors().get(0).getDefaultMessage()
        );
    }
}
