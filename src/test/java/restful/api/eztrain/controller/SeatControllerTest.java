package restful.api.eztrain.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.Collections;
import java.util.List;

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
import restful.api.eztrain.entity.SeatEntity;
import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.model.CoachResponse;
import restful.api.eztrain.model.RegisterSeatRequest;
import restful.api.eztrain.model.SeatResponse;
import restful.api.eztrain.model.UpdateSeatRequest;
import restful.api.eztrain.model.WebResponse;
import restful.api.eztrain.repository.CoachRepository;
import restful.api.eztrain.repository.RoleRepository;
import restful.api.eztrain.repository.SeatRepository;
import restful.api.eztrain.repository.UserRepository;
import restful.api.eztrain.security.JwtUtil;
import restful.api.eztrain.security.SecurityConstants;

@EnableWebMvc
@SpringBootTest
@AutoConfigureMockMvc
public class SeatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CoachRepository coachRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SecurityConstants securityConstants;

    @Autowired
    private ObjectMapper objectMapper;

    private final String email = "test@gmail.com";
    private final String password = "rahasia";

    @BeforeEach
    void setUp() {                
                
        coachRepository.deleteAll();
        seatRepository.deleteAll();
        userRepository.deleteAll();

        RoleEntity role = roleRepository.findByName("ROLE_ADMIN").orElse(null);

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Collections.singletonList(role));
        user.setIsVerified(true);
        user.setIsActive(true);        
        userRepository.save(user);        
    }

    @Test
    void testRegisterSeatSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        RegisterSeatRequest request = new RegisterSeatRequest();
        request.setSeatNumber("1A");

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                post("/api/seats")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<SeatResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(request.getSeatNumber(), response.getData().getSeatNumber());            
        });
    }

    @Test
    void testRegisterSeatBlank() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        RegisterSeatRequest request = new RegisterSeatRequest();
        request.setSeatNumber("");

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                post("/api/seats")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<SeatResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testRegisterSeatDuplicate() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("1A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        RegisterSeatRequest request = new RegisterSeatRequest();
        request.setSeatNumber("1A");

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                post("/api/seats")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<SeatResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());          
        });
    }

    @Test
    void testRegisterSeatInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        RegisterSeatRequest request = new RegisterSeatRequest();
        request.setSeatNumber("1A");

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken + "a";

        mockMvc.perform(
                post("/api/seats")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<SeatResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testRegisterSeatTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        RegisterSeatRequest request = new RegisterSeatRequest();
        request.setSeatNumber("1A");

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() - securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                post("/api/seats")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<SeatResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testRegisterSeatNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        RegisterSeatRequest request = new RegisterSeatRequest();
        request.setSeatNumber("1A");

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        mockMvc.perform(
                post("/api/seats")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                                           
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<SeatResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());           
        });
    }

    @Test
    void testRegisterSeatBadRole() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RoleEntity role = roleRepository.findByName("ROLE_USER").orElse(null);
        
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);

        RegisterSeatRequest request = new RegisterSeatRequest();
        request.setSeatNumber("1A");

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                post("/api/seats")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<SeatResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testGetSeatSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("1A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                get("/api/seats/" + seat.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<SeatResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(seat.getSeatNumber(), response.getData().getSeatNumber());            
        });
    }

    @Test
    void testGetSeatBadId() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("1A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                get("/api/seats/" + seat.getId() + "a")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<SeatResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());           
        });
    }

    @Test
    void testGetSeatNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("1A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                get("/api/seats/111111")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
                WebResponse<SeatResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());           
        });
    }

    @Test
    void testGetSeatInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("1A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken + "a";

        mockMvc.perform(
                get("/api/seats/" + seat.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<SeatResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testGetSeatTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("1A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() - securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                get("/api/seats/" + seat.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<SeatResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());           
        });
    }

    @Test
    void testGetSeatNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("1A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);        

        mockMvc.perform(
                get("/api/seats/" + seat.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                                                                       
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<SeatResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testGetSeatBadRole() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RoleEntity role = roleRepository.findByName("ROLE_NOT_FOUND").orElse(null);
        
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("1A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                get("/api/seats/" + seat.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<SeatResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testUpdateSeatSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("1A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        UpdateSeatRequest request = new UpdateSeatRequest();
        request.setSeatNumber("1B");        

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                patch("/api/seats/" + seat.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                      
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<SeatResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(request.getSeatNumber(), response.getData().getSeatNumber());            
        });
    }

    @Test
    void testUpdateSeatBadId() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("1A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        UpdateSeatRequest request = new UpdateSeatRequest();
        request.setSeatNumber("1B");        

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                patch("/api/seats/" + seat.getId() + "a")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                      
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<SeatResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());           
        });
    }

    @Test
    void testUpdateSeatNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("1A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        UpdateSeatRequest request = new UpdateSeatRequest();
        request.setSeatNumber("1B");        

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                patch("/api/seats/111111")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                      
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
                WebResponse<SeatResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testUpdateSeatInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("1A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        UpdateSeatRequest request = new UpdateSeatRequest();
        request.setSeatNumber("1B");        

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken + "a";

        mockMvc.perform(
                patch("/api/seats/" + seat.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                      
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<SeatResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testUpdateSeatTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("1A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        UpdateSeatRequest request = new UpdateSeatRequest();
        request.setSeatNumber("1B");        

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() - securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                patch("/api/seats/" + seat.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                      
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<SeatResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());           
        });
    }

    @Test
    void testUpdateSeatNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("1A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        UpdateSeatRequest request = new UpdateSeatRequest();
        request.setSeatNumber("1B");        

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);        

        mockMvc.perform(
                patch("/api/seats/" + seat.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                                                                   
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<SeatResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());           
        });
    }

    @Test
    void testUpdateSeatBadRole() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RoleEntity role = roleRepository.findByName("ROLE_USER").orElse(null);
        
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("1A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        UpdateSeatRequest request = new UpdateSeatRequest();
        request.setSeatNumber("1B");        

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                patch("/api/seats/" + seat.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                      
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<SeatResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testDeleteSeatSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("1A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                delete("/api/seats/" + seat.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());            
        });
    }

    @Test
    void testDeleteSeatBadId() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("1A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                delete("/api/seats/" + seat.getId() + "a")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());           
        });
    }

    @Test
    void testDeleteSeatNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("1A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                delete("/api/seats/111111")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
                WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());           
        });
    }

    @Test
    void testDeleteSeatInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("1A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken + "a";

        mockMvc.perform(
                delete("/api/seats/" + seat.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testDeleteSeatTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("1A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() - securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                delete("/api/seats/" + seat.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());           
        });
    }

    @Test
    void testDeleteSeatNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("1A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);        

        mockMvc.perform(
                delete("/api/seats/" + seat.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                                                                       
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testDeleteSeatBadRole() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RoleEntity role = roleRepository.findByName("ROLE_NOT_FOUND").orElse(null);
        
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("1A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                delete("/api/seats/" + seat.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testGetAllSeatSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        for (int i = 0; i < 50; i++) {
            SeatEntity seat = new SeatEntity();
            seat.setSeatNumber(i + "A");
            seat.setUserEntity(user);
            seatRepository.save(seat);
        }

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                get("/api/seats")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<List<SeatResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(10, response.getData().size());
            assertEquals(5, response.getPaging().getTotalPage());
            assertEquals(0, response.getPaging().getCurrentPage());
            assertEquals(10, response.getPaging().getSize());            
        });
    }

    @Test
    void testGetAllSeatInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        for (int i = 0; i < 50; i++) {
            SeatEntity seat = new SeatEntity();
            seat.setSeatNumber(i + "A");
            seat.setUserEntity(user);
            seatRepository.save(seat);
        }

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken + "a";

        mockMvc.perform(
                get("/api/seats")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<List<SeatResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());           
        });
    }

    @Test
    void testGetAllSeatTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        for (int i = 0; i < 50; i++) {
            SeatEntity seat = new SeatEntity();
            seat.setSeatNumber(i + "A");
            seat.setUserEntity(user);
            seatRepository.save(seat);
        }

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() - securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                get("/api/seats")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<List<SeatResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());           
        });
    }

    @Test
    void testGetAllSeatNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);    

        for (int i = 0; i < 50; i++) {
            SeatEntity seat = new SeatEntity();
            seat.setSeatNumber(i + "A");
            seat.setUserEntity(user);
            seatRepository.save(seat);
        }

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);        

        mockMvc.perform(
                get("/api/seats")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                                                                       
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<List<SeatResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());            
        });
    }

    @Test
    void testGetAllSeatBadRole() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RoleEntity role = roleRepository.findByName("ROLE_NOT_FOUND").orElse(null);
        
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);

        for (int i = 0; i < 50; i++) {
            SeatEntity seat = new SeatEntity();
            seat.setSeatNumber(i + "A");
            seat.setUserEntity(user);
            seatRepository.save(seat);
        }

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                get("/api/seats")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<List<SeatResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());           
        });
    }

    @Test
    void testSearchSeatByName() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        

        for (int i = 0; i < 50; i++) {
            SeatEntity seat = new SeatEntity();
            seat.setSeatNumber(i + "A");
            seat.setUserEntity(user);
            seatRepository.save(seat);
        }        

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                get("/api/seats/search")
                        .queryParam("seatNumber", "A")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<List<CoachResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(10, response.getData().size());
            assertEquals(5, response.getPaging().getTotalPage());
            assertEquals(0, response.getPaging().getCurrentPage());
            assertEquals(10, response.getPaging().getSize());
        });        
    }

    @Test
    void testSearchSeatNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        

        for (int i = 0; i < 50; i++) {
            SeatEntity seat = new SeatEntity();
            seat.setSeatNumber(i + "A");
            seat.setUserEntity(user);
            seatRepository.save(seat);
        }        

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                get("/api/seats/search")
                        .queryParam("seatNumber", "B")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<List<CoachResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(0, response.getData().size());
            assertEquals(0, response.getPaging().getTotalPage());
            assertEquals(0, response.getPaging().getCurrentPage());
            assertEquals(10, response.getPaging().getSize());
        });        
    }

    @Test
    void testSearchSeatInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        

        for (int i = 0; i < 50; i++) {
            SeatEntity seat = new SeatEntity();
            seat.setSeatNumber(i + "A");
            seat.setUserEntity(user);
            seatRepository.save(seat);
        }        

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken + "a";

        mockMvc.perform(
                get("/api/seats/search")
                        .queryParam("seatNumber", "A")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<List<CoachResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });        
    }

    @Test
    void testSearchSeatTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        

        for (int i = 0; i < 50; i++) {
            SeatEntity seat = new SeatEntity();
            seat.setSeatNumber(i + "A");
            seat.setUserEntity(user);
            seatRepository.save(seat);
        }        

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() - securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                get("/api/seats/search")
                        .queryParam("seatNumber", "A")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<List<CoachResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });        
    }

    @Test
    void testSearchSeatNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        

        for (int i = 0; i < 50; i++) {
            SeatEntity seat = new SeatEntity();
            seat.setSeatNumber(i + "A");
            seat.setUserEntity(user);
            seatRepository.save(seat);
        }        

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);        

        mockMvc.perform(
                get("/api/seats/search")
                        .queryParam("seatNumber", "A")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                                                                       
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<List<CoachResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });        
    }

    @Test
    void testSearchSeatBadRole() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RoleEntity role = roleRepository.findByName("ROLE_NOT_FOUND").orElse(null);
        
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);

        for (int i = 0; i < 50; i++) {
            SeatEntity seat = new SeatEntity();
            seat.setSeatNumber(i + "A");
            seat.setUserEntity(user);
            seatRepository.save(seat);
        }        

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                get("/api/seats/search")
                        .queryParam("seatNumber", "A")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<List<CoachResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });        
    }
}
