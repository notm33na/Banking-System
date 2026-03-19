package com.virtbank.controller;

import com.virtbank.dto.AuthResponse;
import com.virtbank.dto.LoginRequest;
import com.virtbank.dto.RegisterRequest;
import com.virtbank.entity.Role;
import com.virtbank.entity.User;
import com.virtbank.entity.enums.UserStatus;
import com.virtbank.entity.enums.UserType;
import com.virtbank.repository.RoleRepository;
import com.virtbank.repository.UserRepository;
import com.virtbank.security.JwtUtils;
import com.virtbank.service.EmailService;
import com.virtbank.service.NotificationService;
import com.virtbank.service.TokenBlacklistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final TokenBlacklistService tokenBlacklistService;

    // ═════════════════════════════════════════════════════════════════
    // POST  /api/auth/register
    // ═════════════════════════════════════════════════════════════════
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {

        // 1. Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already registered: " + request.getEmail());
        }

        // 2. Resolve the UserType enum
        UserType userType;
        try {
            userType = UserType.valueOf(request.getUserType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid user type: " + request.getUserType()
                            + ". Must be CUSTOMER, BUSINESS, or ADMIN");
        }

        // 3. Look up the corresponding role
        String roleName = "ROLE_" + userType.name();   // e.g. ROLE_CUSTOMER
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Role not found: " + roleName + ". Please run Liquibase migrations first."));

        // 4. Build and save the user
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .userType(userType)
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .roles(Set.of(role))
                .build();

        User savedUser = userRepository.save(user);

        // 5. Generate JWT
        String token = jwtUtils.generateToken(
                savedUser.getId(), savedUser.getEmail(), roleName);

        // 6. Send welcome email + in-app notification
        emailService.sendWelcomeEmail(savedUser.getEmail(),
                savedUser.getFirstName() + " " + savedUser.getLastName());
        notificationService.createNotification(savedUser.getId(), "ACCOUNT",
                "Welcome to VIRTBANK! Your account is now active.");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, savedUser.getId(),
                        savedUser.getEmail(), roleName));
    }

    // ═════════════════════════════════════════════════════════════════
    // POST  /api/auth/login
    // ═════════════════════════════════════════════════════════════════
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        // 1. Authenticate with Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 2. Get the authenticated principal
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 3. Load full user entity to get userId
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        // 4. Determine primary role
        String role = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_CUSTOMER");

        // 5. Generate JWT
        String token = jwtUtils.generateToken(user.getId(), user.getEmail(), role);

        return ResponseEntity.ok(
                new AuthResponse(token, user.getId(), user.getEmail(), role));
    }

    // ═════════════════════════════════════════════════════════════════
    // POST  /api/auth/logout
    // ═════════════════════════════════════════════════════════════════
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            tokenBlacklistService.blacklist(jwt);
        }
        return ResponseEntity.ok(java.util.Map.of("message", "Logged out successfully"));
    }
}
