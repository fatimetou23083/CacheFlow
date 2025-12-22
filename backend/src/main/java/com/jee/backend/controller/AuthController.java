package com.jee.backend.controller;

import com.jee.backend.model.User;
import com.jee.backend.service.UserService;
import com.jee.backend.dto.LoginRequest;
import com.jee.backend.dto.RegisterRequest;
import com.jee.backend.dto.AuthResponse;
import com.jee.backend.exception.UserAlreadyExistsException;
import com.jee.backend.exception.InvalidCredentialsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:59461", "http://localhost:8081"}, allowCredentials = "true")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpSession session) {
        
        logger.info("Processing login for user: {}", loginRequest.getUsername());
        
        try {
            // Authentification avec AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            
            logger.info("Authentication successful for: {}", authentication.getName());
            
            // Stockage du contexte de sécurité dans la session
            SecurityContextHolder.getContext().setAuthentication(authentication);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            
            // Réponse réussie
            AuthResponse response = AuthResponse.success(
                "Connexion réussie",
                authentication.getName(),
                session.getId()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (BadCredentialsException e) {
            logger.error("LOGIN FAILED - Invalid credentials for user: {}", loginRequest.getUsername());
            throw new InvalidCredentialsException(
                "Les identifiants fournis sont invalides",
                loginRequest.getUsername()
            );
        } catch (Exception e) {
            logger.error("LOGIN FAILED - Unexpected error for user: {}, Exception: {}", 
                        loginRequest.getUsername(), e.getClass().getName(), e);
            throw new InvalidCredentialsException(
                "Erreur lors de l'authentification : " + e.getMessage(),
                loginRequest.getUsername()
            );
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest registerRequest) {
        
        logger.info("Processing registration for user: {}", registerRequest.getUsername());
        
        // Vérifier si l'utilisateur existe déjà
        if (userService.existsByUsername(registerRequest.getUsername())) {
            logger.warn("Registration failed: Username already exists - {}", registerRequest.getUsername());
            throw new UserAlreadyExistsException(
                "Ce nom d'utilisateur est déjà pris",
                "username",
                registerRequest.getUsername()
            );
        }
        
        // Vérifier si l'email existe déjà
        if (userService.existsByEmail(registerRequest.getEmail())) {
            logger.warn("Registration failed: Email already exists - {}", registerRequest.getEmail());
            throw new UserAlreadyExistsException(
                "Cet email est déjà associé à un compte",
                "email",
                registerRequest.getEmail()
            );
        }
        
        // Créer le nouvel utilisateur
        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setEmail(registerRequest.getEmail());
        newUser.setPassword(registerRequest.getPassword());
        newUser.setRole(registerRequest.getRole() != null ? registerRequest.getRole() : "USER");
        
        // Sauvegarder l'utilisateur
        User savedUser = userService.save(newUser);
        logger.info("User registered successfully: {}", savedUser.getUsername());
        
        // Réponse réussie
        AuthResponse response = AuthResponse.success(
            "Enregistrement réussi ! Vous pouvez maintenant vous connecter.",
            savedUser.getUsername()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpServletRequest request) {
        logger.info("Processing logout request");
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
            logger.info("Session invalidated successfully");
        }
        
        SecurityContextHolder.clearContext();
        
        AuthResponse response = AuthResponse.success(
            "Déconnexion réussie",
            null
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(Authentication authentication) {
        logger.info("Checking current user session");
        
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("No authenticated user found");
            throw new InvalidCredentialsException("Aucun utilisateur authentifié");
        }
        
        logger.info("Current authenticated user: {}", authentication.getName());
        
        AuthResponse response = new AuthResponse();
        response.setSuccess(true);
        response.setMessage("Utilisateur authentifié");
        response.setUsername(authentication.getName());
        
        return ResponseEntity.ok(response);
    }
}
