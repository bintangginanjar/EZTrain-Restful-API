package restful.api.eztrain.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

import restful.api.eztrain.entity.CoachEntity;
import restful.api.eztrain.entity.CoachTypeEntity;
import restful.api.eztrain.entity.RoleEntity;
import restful.api.eztrain.entity.TrainEntity;
import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.model.CoachResponse;
import restful.api.eztrain.model.RegisterCoachRequest;
import restful.api.eztrain.model.RegisterTrainRequest;
import restful.api.eztrain.model.TrainResponse;
import restful.api.eztrain.model.WebResponse;
import restful.api.eztrain.repository.CoachRepository;
import restful.api.eztrain.repository.CoachTypeRepository;
import restful.api.eztrain.repository.RoleRepository;
import restful.api.eztrain.repository.SeatRepository;
import restful.api.eztrain.repository.TrainRepository;
import restful.api.eztrain.repository.UserRepository;
import restful.api.eztrain.security.JwtUtil;
import restful.api.eztrain.security.SecurityConstants;

@EnableWebMvc
@SpringBootTest
@AutoConfigureMockMvc
public class CoachControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private CoachRepository coachRepository; 
    
    @Autowired
    private CoachTypeRepository coachTypeRepository;

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

    private final String eksCoachName = "Eksekutif 1";
    private final Integer eksCoachNumber = 1;
    private final String eksCoachType = "Eksekutif";

    private final String panCoachName = "Panomaric 1";
    private final Integer panCoachNumber = 1;
    private final String panCoachType = "Panoramic";

    @BeforeEach
    void setUp() {                
        
        seatRepository.deleteAll();
        trainRepository.deleteAll();
        coachRepository.deleteAll();
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
    void testRegisterCoachSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        RegisterCoachRequest request = new RegisterCoachRequest();
        request.setCoachName(eksCoachName);
        request.setCoachTypeId(Long.toString(coachType.getId()));
        request.setCoachNumber(eksCoachNumber);               

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
                post("/api/coaches")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<CoachResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(request.getCoachName(), response.getData().getCoachName());
            assertEquals(request.getCoachNumber(), response.getData().getCoachNumber());
            assertEquals(request.getCoachTypeId(), Long.toString(response.getData().getCoachTypeId()));
            assertEquals(coachType.getName(), response.getData().getCoachTypeName());
        });
    }

    @Test
    void testRegisterCoachBlank() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        

        RegisterCoachRequest request = new RegisterCoachRequest();
        request.setCoachName("");
        request.setCoachTypeId("");
        request.setCoachNumber(null);           

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
                post("/api/coaches")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<CoachResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testRegisterCoachInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        RegisterCoachRequest request = new RegisterCoachRequest();
        request.setCoachName(eksCoachName);
        request.setCoachTypeId(Long.toString(coachType.getId()));
        request.setCoachNumber(eksCoachNumber);               

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
                post("/api/coaches")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<CoachResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testRegisterCoachTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        RegisterCoachRequest request = new RegisterCoachRequest();
        request.setCoachName(eksCoachName);
        request.setCoachTypeId(Long.toString(coachType.getId()));
        request.setCoachNumber(eksCoachNumber);               

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
                post("/api/coaches")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<CoachResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testRegisterCoachNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        RegisterCoachRequest request = new RegisterCoachRequest();
        request.setCoachName(eksCoachName);
        request.setCoachTypeId(Long.toString(coachType.getId()));
        request.setCoachNumber(eksCoachNumber);               

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);        

        mockMvc.perform(
                post("/api/coaches")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                                             
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<CoachResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testRegisterCoachBadRole() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RoleEntity role = roleRepository.findByName("ROLE_USER").orElse(null);
        
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        RegisterCoachRequest request = new RegisterCoachRequest();
        request.setCoachName(eksCoachName);
        request.setCoachTypeId(Long.toString(coachType.getId()));
        request.setCoachNumber(eksCoachNumber);               

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
                post("/api/coaches")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<CoachResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testGetCoachSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

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
                get("/api/coaches/" + coach.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<CoachResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(coach.getId(), response.getData().getId());            
            assertEquals(coach.getCoachName(), response.getData().getCoachName());
            assertEquals(coach.getCoachNumber(), response.getData().getCoachNumber());
            assertEquals(coachType.getId(), response.getData().getCoachTypeId());
            assertEquals(coachType.getName(), response.getData().getCoachTypeName());
        });
    }

    @Test
    void testGetCoachBadId() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

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
                get("/api/coaches/" + coach.getId() + "a")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<CoachResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testGetCoachNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

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
                get("/api/coaches/111111")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
                WebResponse<CoachResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testGetCoachInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

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
                get("/api/coaches/" + coach.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<CoachResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testGetCoachTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

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
                get("/api/coaches/" + coach.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<CoachResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testGetCoachNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        mockMvc.perform(
                get("/api/coaches/" + coach.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                                                                     
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<CoachResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testGetCoachBadRole() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RoleEntity role = roleRepository.findByName("ROLE_USER").orElse(null);
        
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

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
                get("/api/coaches/" + coach.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<CoachResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }
}
