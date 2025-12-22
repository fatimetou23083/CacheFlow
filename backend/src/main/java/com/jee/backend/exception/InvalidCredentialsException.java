package com.jee.backend.exception;

/**
 * Exception levée quand l'authentification échoue
 * (username/password invalides)
 */
public class InvalidCredentialsException extends RuntimeException {

    private String username;
    private int attemptNumber;

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, String username) {
        super(message);
        this.username = username;
    }

    public InvalidCredentialsException(String message, String username, int attemptNumber) {
        super(message);
        this.username = username;
        this.attemptNumber = attemptNumber;
    }

    public String getUsername() {
        return username;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }
}
