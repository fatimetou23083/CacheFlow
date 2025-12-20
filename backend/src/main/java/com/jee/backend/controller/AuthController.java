package com.jee.backend.controller;

import com.jee.backend.model.User;
import com.jee.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:59461", "http://localhost:8081"}, allowCredentials = "true")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest, HttpSession session) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");

            // Log parameters (without logging password)
            System.out.println("Processing login request. Keys: " + loginRequest.keySet());
            if (username == null || username.trim().isEmpty()) {
                System.err.println("Login failed: Username is missing or empty.");
                return ResponseEntity.badRequest().body("Error: Username is required.");
            }
            if (password == null || password.trim().isEmpty()) {
                System.err.println("Login failed: Password is missing or empty.");
                return ResponseEntity.badRequest().body("Error: Password is required.");
            }

            System.out.println("Processing login for user: " + username);
            
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
            );
            
            System.out.println("Authentication successful for: " + authentication.getName());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("username", authentication.getName());
            response.put("sessionId", session.getId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("LOGIN FAILED for user: " + loginRequest.get("username"));
            System.err.println("Exception Class: " + e.getClass().getName());
            System.err.println("Reason: " + e.getMessage());
            e.printStackTrace(); // Print full stack trace to logs
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Unauthorized");
            errorResponse.put("message", "Connexion échouée : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        System.out.println("Processing registration for user: " + user.getUsername());
        if (userService.existsByUsername(user.getUsername())) {
            System.out.println("Registration failed: Username already taken");
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }

        userService.save(user);
        System.out.println("User registered successfully: " + user.getUsername());
        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully!");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Map<String, Object> response = new HashMap<>();
        response.put("username", authentication.getName());
        response.put("authenticated", true);
        return ResponseEntity.ok(response);
    }
}
