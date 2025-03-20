package com.gdg.Todak.member.controller.advice;

import com.gdg.Todak.common.domain.ApiResponse;
import com.gdg.Todak.member.exception.EncryptionException;
import com.gdg.Todak.member.exception.UnauthorizedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MemberControllerAdvice {

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorizedException.class)
    public ApiResponse<Exception> loginFailed(UnauthorizedException e) {
        return ApiResponse.of(
                HttpStatus.UNAUTHORIZED,
                e.getMessage()
        );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(EncryptionException.class)
    public ApiResponse<Exception> encryptionFailed(EncryptionException e) {
        return ApiResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.getMessage()
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

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiResponse<Object> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        return ApiResponse.of(
                HttpStatus.CONFLICT,
                e.getMostSpecificCause().getMessage()
        );
    }
}
