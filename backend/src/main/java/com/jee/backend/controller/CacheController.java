package com.jee.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@RestController
@RequestMapping("/api/cache")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CacheController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CacheManager cacheManager;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            Properties info = redisTemplate.getConnectionFactory().getConnection().info();
            stats.put("uptime", info.getProperty("uptime_in_seconds"));
            stats.put("connected_clients", info.getProperty("connected_clients"));
            stats.put("used_memory_human", info.getProperty("used_memory_human"));
            stats.put("total_keys", redisTemplate.keys("*").size());
            
            // Simulating hits/misses since Redis standalone doesn't fully expose keyspace hits easily without config
            // In real world, we'd parse 'keyspace_hits' and 'keyspace_misses' from info
            stats.put("hits", info.getProperty("keyspace_hits", "0"));
            stats.put("misses", info.getProperty("keyspace_misses", "0"));

        } catch (Exception e) {
            stats.put("error", "Could not retrieve Redis stats");
        }
        
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/clear/{cacheName}")
    public ResponseEntity<?> clearCache(@PathVariable String cacheName) {
        if (cacheManager.getCache(cacheName) != null) {
            cacheManager.getCache(cacheName).clear();
            return ResponseEntity.ok().body(Map.of("message", "Cache " + cacheName + " cleared"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Cache not found"));
    }
    
    @PostMapping("/clear-all")
    public ResponseEntity<?> clearAllCaches() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        return ResponseEntity.ok().body(Map.of("message", "All Redis keys cleared"));
    }
}
