package com.jee.backend.dto;

/**
 * DTO pour les réponses d'authentification
 * Standardise la réponse après login/register/logout
 */
public class AuthResponse {

    private boolean success;
    private String message;
    private String username;
    private String sessionId;
    private long timestamp;

    public AuthResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public AuthResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public AuthResponse(boolean success, String message, String username, String sessionId) {
        this.success = success;
        this.message = message;
        this.username = username;
        this.sessionId = sessionId;
        this.timestamp = System.currentTimeMillis();
    }

    // Constructeurs statiques pour faciliter la création
    public static AuthResponse success(String message, String username, String sessionId) {
        return new AuthResponse(true, message, username, sessionId);
    }

    public static AuthResponse success(String message, String username) {
        return new AuthResponse(true, message, username, null);
    }

    public static AuthResponse error(String message) {
        return new AuthResponse(false, message);
    }

    // Getters et Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "AuthResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", username='" + username + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
