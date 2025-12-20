package com.jee.backend.actuator;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;

@Endpoint(id = "redis")
public class RedisEndpoint {

    private final RedisConnectionFactory redisConnectionFactory;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheManager cacheManager;

    public RedisEndpoint(RedisConnectionFactory redisConnectionFactory, 
                        RedisTemplate<String, Object> redisTemplate,
                        CacheManager cacheManager) {
        this.redisConnectionFactory = redisConnectionFactory;
        this.redisTemplate = redisTemplate;
        this.cacheManager = cacheManager;
    }

    @ReadOperation
    public Map<String, Object> redisInfo() {
        Map<String, Object> info = new HashMap<>();
        
        try {
            // Test Redis connection
            redisTemplate.opsForValue().set("health-check", "ok");
            String result = (String) redisTemplate.opsForValue().get("health-check");
            redisTemplate.delete("health-check");
            
            info.put("status", "connected");
            info.put("connectionTest", result != null ? "success" : "failed");
            
            // Add cache information
            if (cacheManager instanceof RedisCacheManager) {
                Map<String, Object> cacheInfo = new HashMap<>();
                for (String cacheName : cacheManager.getCacheNames()) {
                    cacheInfo.put(cacheName, "configured");
                }
                info.put("caches", cacheInfo);
            }
            
            info.put("message", "Redis is connected and operational");
        } catch (Exception e) {
            info.put("status", "error");
            info.put("message", e.getMessage());
        }
        
        return info;
    }
}
