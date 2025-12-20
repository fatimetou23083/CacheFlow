package com.jee.backend.config;

import com.jee.backend.model.User;
import com.jee.backend.repository.UserRepository;
import com.jee.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Checking for default admin user...");
        if (!userService.existsByUsername("admin")) {
            System.out.println("Admin user not found, creating default admin user...");
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin");
            admin.setEmail("admin@cacheflow.com");
            admin.setRole("ADMIN");
            
            userService.save(admin);
            System.out.println("Default admin user created successfully: admin / admin");
        } else {
            System.out.println("Admin user already exists. Skipping initialization.");
        }

        if (userRepository.count() > 0) {
            System.out.println("Existing users in database:");
            userRepository.findAll().forEach(u -> 
                System.out.println(" - " + u.getUsername() + " (Role: " + u.getRole() + ")")
            );
        }

        try {
            System.out.println("Checking Redis connection...");
            String ping = redisTemplate.getConnectionFactory().getConnection().ping();
            System.out.println("Redis connection successful: " + ping);
        } catch (Exception e) {
            System.err.println("CRITICAL: Redis connection failed! Caching will not work. Error: " + e.getMessage());
        }
    }
}
