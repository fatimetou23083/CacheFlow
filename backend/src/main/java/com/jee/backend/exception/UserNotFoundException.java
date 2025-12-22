package com.jee.backend.exception;

/**
 * Exception levée quand un utilisateur n'est pas trouvé
 */
public class UserNotFoundException extends RuntimeException {

    private String username;
    private String userId;

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, String username) {
        super(message);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public String getUserId() {
        return userId;
    }
}
