package com.smartcourier.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcourier.auth.dto.AuthResponse;
import com.smartcourier.auth.dto.LoginRequest;
import com.smartcourier.auth.dto.SignupRequest;
import com.smartcourier.auth.dto.UserSummaryResponse;
import com.smartcourier.auth.security.JwtService;
import com.smartcourier.auth.service.AuthService;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(com.smartcourier.auth.exception.GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @Test
    void signupShouldReturnCreated() throws Exception {
        when(authService.signup(any(SignupRequest.class)))
                .thenReturn(new AuthResponse("jwt", "aman@example.com", "Aman", Set.of("ROLE_CUSTOMER")));

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new SignupRequest("Aman", "aman@example.com", "Password@1", "9876543210"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt"));
    }

    @Test
    void loginShouldReturnOk() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new AuthResponse("jwt", "admin@smartcourier.com", "System Admin", Set.of("ROLE_ADMIN")));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin@smartcourier.com", "Admin@123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@smartcourier.com"));
    }

    @Test
    void meShouldReturnCurrentUser() throws Exception {
        when(authService.getCurrentUser("admin@smartcourier.com"))
                .thenReturn(new UserSummaryResponse(1L, "System Admin", "admin@smartcourier.com", "9999999999", true, Set.of("ROLE_ADMIN")));

        mockMvc.perform(get("/api/v1/auth/me")
                        .principal(new TestingAuthenticationToken("admin@smartcourier.com", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("System Admin"));
    }
}
