package se.seb.bundleservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import se.seb.bundleservice.exception.ApiError;
import se.seb.bundleservice.exception.NotFoundException;
import se.seb.bundleservice.exception.UnmatchedConditionsException;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS;

@ControllerAdvice
public class ControllerErrorAdvice {

    @ExceptionHandler(value = {UnmatchedConditionsException.class})
    public ResponseEntity<ApiError> handleUnmatchedConditionsException(UnmatchedConditionsException unmatchedConditionsException) {
        return ResponseEntity.status(UNAVAILABLE_FOR_LEGAL_REASONS)
                .body(getApiError(unmatchedConditionsException.getMessage(), UNAVAILABLE_FOR_LEGAL_REASONS));
    }

    @ExceptionHandler(value = {NotFoundException.class})
    public ResponseEntity<ApiError> handleNotFoundException(NotFoundException notFoundException) {
        return ResponseEntity.status(NOT_FOUND)
                .body(getApiError(notFoundException.getMessage(), NOT_FOUND));
    }

    private ApiError getApiError(String message, HttpStatus httpStatus) {
        return new ApiError(message, httpStatus);
    }
}
