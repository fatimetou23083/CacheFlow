package com.jee.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jee.backend.model.Notification;
import com.jee.backend.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ChannelTopic topic;
    
    @Autowired
    private ObjectMapper objectMapper;

    public Notification createAndPublish(String message, String type, String userId) {
        Notification notification = new Notification(message, type, userId);
        
        // 1. Save to DB
        Notification saved = notificationRepository.save(notification);
        
        // 2. Publish to Redis
        try {
            String jsonEntry = objectMapper.writeValueAsString(saved);
            redisTemplate.convertAndSend(topic.getTopic(), jsonEntry);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        
        return saved;
    }

    public List<Notification> getAll() {
        return notificationRepository.findAll();
    }
    
    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByTimestampDesc(userId);
    }
    
    public List<Notification> getBroadcastNotifications() {
        return notificationRepository.findByUserIdIsNullOrderByTimestampDesc();
    }
}
