package com.news.digest.app.exception;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
public class BadRequestException extends  RuntimeException{

    private final HttpStatus status;
    private final String message;

    public BadRequestException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
        this.message = message;
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.BAD_REQUEST;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    // Common factory methods
    public static BadRequestException duplicateResource(String resource, String field, Object value) {
        return new BadRequestException(resource + " already exists with " + field + ": '" + value + "'");
    }

    public static BadRequestException invalidInput(String field) {
        return new BadRequestException("Invalid input for field: " + field);
    }

    public static BadRequestException missingRequiredField(String field) {
        return new BadRequestException("Required field is missing: " + field);
    }

    public static BadRequestException invalidOperation(String reason) {
        return new BadRequestException("Invalid operation: " + reason);
    }

}
