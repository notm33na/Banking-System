package com.virtbank.service;

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
import com.virtbank.controller.AuthController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtils jwtUtils;
    @Mock private EmailService emailService;
    @Mock private NotificationService notificationService;

    @InjectMocks private AuthController authController;

    private RegisterRequest registerRequest;
    private Role customerRole;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setEmail("john@test.com");
        registerRequest.setPassword("P@ssword1");
        registerRequest.setPhone("1234567890");
        registerRequest.setUserType("CUSTOMER");

        customerRole = new Role();
        customerRole.setId(1L);
        customerRole.setName("ROLE_CUSTOMER");
    }

    // ── Registration Tests ──────────────────────────────────────────

    @Test
    void register_success_returnsJwtAndCreatesUser() {
        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_CUSTOMER")).thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode("P@ssword1")).thenReturn("$2a$hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtUtils.generateToken(eq(1L), eq("john@test.com"), eq("ROLE_CUSTOMER")))
                .thenReturn("jwt-token-123");

        ResponseEntity<AuthResponse> response = authController.register(registerRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("jwt-token-123", response.getBody().getToken());
        assertEquals("john@test.com", response.getBody().getEmail());

        // Verify password was hashed
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("$2a$hashed", userCaptor.getValue().getPasswordHash());

        // Verify welcome email sent
        verify(emailService).sendWelcomeEmail("john@test.com", "John Doe");
        verify(notificationService).createNotification(eq(1L), eq("ACCOUNT"), anyString());
    }

    @Test
    void register_duplicateEmail_throwsException() {
        when(userRepository.existsByEmail("john@test.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authController.register(registerRequest));
        verify(userRepository, never()).save(any());
    }

    // ── Login Tests ──────────────────────────────────────────────────

    @Test
    void login_success_returnsJwt() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("john@test.com");
        loginRequest.setPassword("P@ssword1");

        Authentication auth = mock(Authentication.class);
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "john@test.com", "hashed", List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);

        User user = User.builder().id(1L).email("john@test.com")
                .userType(UserType.CUSTOMER).status(UserStatus.ACTIVE).build();
        when(userRepository.findByEmail("john@test.com")).thenReturn(Optional.of(user));
        when(jwtUtils.generateToken(1L, "john@test.com", "ROLE_CUSTOMER")).thenReturn("jwt-login");

        ResponseEntity<AuthResponse> response = authController.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwt-login", response.getBody().getToken());
    }

    @Test
    void login_wrongPassword_throwsBadCredentials() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("john@test.com");
        loginRequest.setPassword("wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authController.login(loginRequest));
    }

    @Test
    void login_nonExistentEmail_throwsException() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("nobody@test.com");
        loginRequest.setPassword("any");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authController.login(loginRequest));
    }
}
