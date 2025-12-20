package com.jee.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.io.Serializable;
import java.time.LocalDateTime;

@Document(collection = "notifications")
public class Notification implements Serializable {
    
    @Id
    private String id;

    private String message;
    private String type; // INFO, ALERT, SUCCESS
    private String userId; // Optional, null for broadcast
    private LocalDateTime timestamp;
    private boolean read;

    public Notification() {
        this.timestamp = LocalDateTime.now();
        this.read = false;
    }

    public Notification(String message, String type, String userId) {
        this.message = message;
        this.type = type;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
        this.read = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}
