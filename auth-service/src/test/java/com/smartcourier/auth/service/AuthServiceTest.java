package com.smartcourier.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.smartcourier.auth.dto.LoginRequest;
import com.smartcourier.auth.dto.SignupRequest;
import com.smartcourier.auth.entity.Role;
import com.smartcourier.auth.entity.User;
import com.smartcourier.auth.exception.ResourceConflictException;
import com.smartcourier.auth.exception.UnauthorizedException;
import com.smartcourier.auth.repository.UserRepository;
import com.smartcourier.auth.security.JwtService;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void signupShouldPersistCustomer() {
        SignupRequest request = new SignupRequest("Aman", "aman@example.com", "Password@1", "9876543210");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded");
        when(jwtService.generateToken(request.email(), Set.of("ROLE_CUSTOMER"))).thenReturn("jwt");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        var response = authService.signup(request);

        assertEquals("jwt", response.token());
        assertEquals("aman@example.com", response.email());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signupShouldRejectDuplicateEmail() {
        SignupRequest request = new SignupRequest("Aman", "aman@example.com", "Password@1", "9876543210");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(ResourceConflictException.class, () -> authService.signup(request));
    }

    @Test
    void loginShouldFailForBadPassword() {
        User user = new User();
        user.setEmail("aman@example.com");
        user.setPassword("encoded");
        user.getRoles().add(Role.ROLE_CUSTOMER);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(new LoginRequest(user.getEmail(), "wrong")));
    }
}

