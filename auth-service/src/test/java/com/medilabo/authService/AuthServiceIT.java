package com.medilabo.authService;

import com.medilabo.authService.model.User;
import com.medilabo.authService.repository.UserRepository;
import com.medilabo.authService.service.AuthService;
import com.medilabo.authService.util.JwtUtil;
import com.medilabo.authService.util.KeyUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthServiceIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void login_shouldReturnSuccess_whenCredentialsAreValid() throws Exception {
        String username = "testuser";
        String password = "password123";
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        user.setPassword(new Argon2PasswordEncoder(16, 32, 2, 65536, 1).encode(password));

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(any(), any(), any())).thenReturn("mocked.jwt.token");

        MvcResult result = mockMvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("mocked.jwt.token"))
                .andReturn();

        assertNotNull(result.getResponse().getContentAsString());
    }

    @Test
    void login_shouldReturnUserNotFound_whenUserDoesNotExist() throws Exception {
        String username = "unknownuser";
        String password = "password123";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("User not found"));
    }

    @Test
    void login_shouldReturnUserNotFound_whenPasswordIsIncorrect() throws Exception {
        String username = "testuser";
        String password = "wrongpassword";
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        user.setPassword(new Argon2PasswordEncoder(16, 32, 2, 65536, 1).encode("correctpassword"));

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("User not found"));
    }

    @Test
    void login_shouldReturnError_whenExceptionOccurs() throws Exception {
        String username = "testuser";
        String password = "password123";

        when(userRepository.findByUsername(username)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Error during login"));
    }
}