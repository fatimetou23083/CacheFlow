package com.jee.backend.service;

import com.jee.backend.model.User;
import com.jee.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("UserDetailsService looking up user: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("UserDetailsService: User not found -> {}", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });

        logger.info("UserDetailsService found user: {}", user.getUsername());
        
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            logger.error("CRITICAL: Found user document but username is null or empty! ID: {}", user.getId());
            throw new UsernameNotFoundException("User found but has invalid data (username missing)");
        }
        if (user.getPassword() == null) {
            logger.error("CRITICAL: Found user document but password is null! Username: {}", user.getUsername());
            throw new UsernameNotFoundException("User found but has invalid data (password missing)");
        }

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(), 
            user.getPassword(), 
            new ArrayList<>()
        );
    }

    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    
    public Boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
