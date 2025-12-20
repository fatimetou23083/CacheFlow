package com.jee.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jee.backend.model.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class RedisMessageSubscriber implements MessageListener {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String msg = new String(message.getBody());
            // Assuming the message is a JSON string of Notification
            // But we actually just sent the message string or the serialized object? 
            // Better to send the whole notification object as JSON
            
            // If we receive a JSON string
            // System.out.println("Message received: " + msg);
            
            // Forward to WebSocket
            // Convert byte[] to String/Object
            // Here we assume it's just the notification payload
            
            messagingTemplate.convertAndSend("/topic/notifications", msg);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
