package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ControllerExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler
    public ErrorDto exceptionHandler(NotFoundException e) {
        log.info("NotFoundException: {}", e.getMessage());
        return new ErrorDto(HttpStatus.NOT_FOUND.name(), "The required object was not found.", e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler
    public ErrorDto exceptionHandler(AccessDeniedException e) {
        log.info("AccessDeniedException: {}", e.getMessage());
        return new ErrorDto(HttpStatus.CONFLICT.name(), "Object modification is not available.", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorDto exceptionHandler(DataIntegrityViolationException e) {
        log.info("DataIntegrityViolationException: {}", e.getMessage());
        return new ErrorDto(HttpStatus.CONFLICT.name(), "Data integrity violation exception.", e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ErrorDto exceptionHandler(MethodArgumentNotValidException e) {
        log.info("MethodArgumentNotValidException: {}", e.getMessage());
        return new ErrorDto(HttpStatus.BAD_REQUEST.name(), "Validation fail.", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDto exceptionHandler(MissingServletRequestParameterException e) {
        log.info("MissingServletRequestParameterException: {}", e.getMessage());
        return new ErrorDto(HttpStatus.BAD_REQUEST.name(), "The required parameter not present.", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorDto exceptionHandler(IllegalArgumentException e) {
        log.info("IllegalArgumentException: {}", e.getMessage());
        return new ErrorDto(HttpStatus.BAD_REQUEST.name(), "Incoming argument not valid.", e.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    public ErrorDto exceptionHandler(Exception e) {
        log.error("Exception: {}", e.getMessage(), e);
        return new ErrorDto(HttpStatus.INTERNAL_SERVER_ERROR.name(), "The required object was not found.", e.getMessage());
    }
}