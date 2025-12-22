package com.jee.backend.exception;

/**
 * Exception levée quand une validation échoue
 */
public class ValidationException extends RuntimeException {

    private String field;
    private String message;

    public ValidationException(String message) {
        super(message);
        this.message = message;
    }

    public ValidationException(String message, String field) {
        super(message);
        this.message = message;
        this.field = field;
    }

    public String getField() {
        return field;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
