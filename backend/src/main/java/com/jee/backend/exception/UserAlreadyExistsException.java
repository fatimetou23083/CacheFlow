package com.jee.backend.exception;

/**
 * Exception levée quand un utilisateur existe déjà
 */
public class UserAlreadyExistsException extends RuntimeException {

    private String field;  // username ou email
    private String value;  // valeur qui existe déjà

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String message, String field, String value) {
        super(message);
        this.field = field;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }
}
