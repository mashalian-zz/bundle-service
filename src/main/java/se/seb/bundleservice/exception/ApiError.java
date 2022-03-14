package se.seb.bundleservice.exception;

import lombok.Value;
import org.springframework.http.HttpStatus;

@Value
public class ApiError {
    String message;
    HttpStatus httpStatus;
}
