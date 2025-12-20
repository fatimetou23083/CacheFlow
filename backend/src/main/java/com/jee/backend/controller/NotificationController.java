package com.jee.backend.controller;

import com.jee.backend.model.Notification;
import com.jee.backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<Notification> sendNotification(@RequestBody Map<String, Object> payload) {
        String message = (String) payload.get("message");
        String type = (String) payload.getOrDefault("type", "INFO");
        String userId = payload.containsKey("userId") ? payload.get("userId").toString() : null;
        
        Notification notification = notificationService.createAndPublish(message, type, userId);
        return ResponseEntity.ok(notification);
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getAll() {
        return ResponseEntity.ok(notificationService.getAll());
    }
}
