package restful.api.eztrain.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import restful.api.eztrain.entity.RoleEntity;
import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.model.ForgotPasswordRequest;
import restful.api.eztrain.model.ForgotPasswordResponse;
import restful.api.eztrain.model.LoginUserRequest;
import restful.api.eztrain.model.ResetPasswordRequest;
import restful.api.eztrain.model.TokenResponse;
import restful.api.eztrain.model.WebResponse;
import restful.api.eztrain.repository.RoleRepository;
import restful.api.eztrain.repository.UserRepository;
import restful.api.eztrain.security.JwtUtil;
import restful.api.eztrain.security.SecurityConstants;

@EnableWebMvc
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private final String email = "test@gmail.com";
    private final String password = "rahasia";

    @BeforeEach
    void setUp() {                

        userRepository.deleteAll();

        RoleEntity role = roleRepository.findByName("ROLE_USER").orElse(null);

        UserEntity user = new UserEntity();
        user.setEmail(email);        
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Collections.singletonList(role));
        user.setIsVerified(true);
        user.setIsActive(true);        
        userRepository.save(user);

    }

    @Test
    void testLoginSuccess() throws Exception {
        LoginUserRequest request = new LoginUserRequest();
        request.setEmail(email);
        request.setPassword(password);        

        mockMvc.perform(
                post("/api/auth/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<TokenResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(request.getEmail(), response.getData().getEmail());
        });
    }

    @Test
    void testLoginFailed() throws Exception {
        LoginUserRequest request = new LoginUserRequest();
        request.setEmail("testemail");
        request.setPassword("testuser");        

        mockMvc.perform(
                post("/api/auth/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<TokenResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testLogoutSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        
        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;
        
        mockMvc.perform(
            delete("/api/auth/logout")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)   
                        .header("Authorization", mockBearerToken)                                            
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<TokenResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
        });
    }

    @Test
    void testLogoutInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        
        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken + "a";
        
        mockMvc.perform(
            delete("/api/auth/logout")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)   
                        .header("Authorization", mockBearerToken)                                            
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<TokenResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testLogoutNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        
        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);        
        
        mockMvc.perform(
            delete("/api/auth/logout")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                           
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<TokenResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testForgotPasswordSuccess() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail(email);               

        mockMvc.perform(
                post("/api/auth/forgot-password")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<ForgotPasswordResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(request.getEmail(), response.getData().getEmail());
            assertNotNull(response.getData().getToken());
        });
    }

    @Test
    void testForgotPasswordNotFound() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail(email + "notfound");               

        mockMvc.perform(
                post("/api/auth/forgot-password")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                        
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
                WebResponse<ForgotPasswordResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testForgotPasswordBlank() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("");               

        mockMvc.perform(
                post("/api/auth/forgot-password")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<ForgotPasswordResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testResetPasswordSuccess() throws Exception {
        String token = UUID.randomUUID().toString();

        UserEntity user = userRepository.findByEmail(email).orElse(null);
        user.setToken(token);
        userRepository.save(user);

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail(email);
        request.setToken(token);
        request.setPassword(password + "new");       

        mockMvc.perform(
                post("/api/auth/reset-password")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
        });
    }

    @Test
    void testResetPasswordBlank() throws Exception {
        String token = UUID.randomUUID().toString();

        UserEntity user = userRepository.findByEmail(email).orElse(null);
        user.setToken(token);
        userRepository.save(user);

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail(email);
        request.setToken("");
        request.setPassword("");       

        mockMvc.perform(
                post("/api/auth/reset-password")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testResetPasswordNotFound() throws Exception {
        String token = UUID.randomUUID().toString();

        UserEntity user = userRepository.findByEmail(email).orElse(null);
        user.setToken(token);
        userRepository.save(user);

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail(email + "notfound");
        request.setToken(token + "notfound");
        request.setPassword(password + "new");       

        mockMvc.perform(
                post("/api/auth/reset-password")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                        
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
                WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }
}
