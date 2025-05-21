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
import restful.api.eztrain.entity.SeatEntity;
import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.model.CoachResponse;
import restful.api.eztrain.model.RegisterCoachRequest;
import restful.api.eztrain.model.UpdateCoachRequest;
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
                
        trainRepository.deleteAll();
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
    void testRegisterCoachDuplicate() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

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
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<CoachResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
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

    @Test
    void testUpdateCoachSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

        UpdateCoachRequest request = new UpdateCoachRequest();
        request.setCoachName(eksCoachName + " updated");
        request.setCoachTypeId(Long.toString(coachType.getId()));
        request.setCoachNumber(eksCoachNumber + 10);   

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
                patch("/api/coaches/" + coach.getId())
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
    void testUpdateCoachDuplicate() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity eksekutifCoach = coachTypeRepository.findByName(eksCoachType).orElse(null);
        CoachTypeEntity panoramicCoach = coachTypeRepository.findByName(panCoachType).orElse(null);

        CoachEntity eksCoach = new CoachEntity();
        eksCoach.setCoachName(eksCoachName);
        eksCoach.setCoachNumber(eksCoachNumber);
        eksCoach.setCoachTypeEntity(eksekutifCoach);
        eksCoach.setIsActive(true);
        eksCoach.setUserEntity(user);
        coachRepository.save(eksCoach);

        CoachEntity panCoach = new CoachEntity();
        panCoach.setCoachName(panCoachName);
        panCoach.setCoachNumber(panCoachNumber);
        panCoach.setCoachTypeEntity(panoramicCoach);
        panCoach.setIsActive(true);
        panCoach.setUserEntity(user);
        coachRepository.save(panCoach);

        UpdateCoachRequest request = new UpdateCoachRequest();
        request.setCoachName(panCoachName);
        request.setCoachTypeId(Long.toString(panoramicCoach.getId()));
        request.setCoachNumber(eksCoachNumber + 10);   

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
                patch("/api/coaches/" + eksCoach.getId())
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
    void testUpdateCoachBadId() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

        UpdateCoachRequest request = new UpdateCoachRequest();
        request.setCoachName(eksCoachName + " updated");
        request.setCoachTypeId(Long.toString(coachType.getId()));
        request.setCoachNumber(eksCoachNumber + 10);   

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
                patch("/api/coaches/" + coach.getId() + "a")
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
    void testUpdateCoachNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

        UpdateCoachRequest request = new UpdateCoachRequest();
        request.setCoachName(eksCoachName + " updated");
        request.setCoachTypeId(Long.toString(coachType.getId()));
        request.setCoachNumber(eksCoachNumber + 10);   

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
                patch("/api/coaches/111111")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
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
    void testUpdateCoachInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

        UpdateCoachRequest request = new UpdateCoachRequest();
        request.setCoachName(eksCoachName + " updated");
        request.setCoachTypeId(Long.toString(coachType.getId()));
        request.setCoachNumber(eksCoachNumber + 10);   

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
                patch("/api/coaches/" + coach.getId())
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
    void testUpdateCoachTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

        UpdateCoachRequest request = new UpdateCoachRequest();
        request.setCoachName(eksCoachName + " updated");
        request.setCoachTypeId(Long.toString(coachType.getId()));
        request.setCoachNumber(eksCoachNumber + 10);   

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
                patch("/api/coaches/" + coach.getId())
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
    void testUpdateCoachNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

        UpdateCoachRequest request = new UpdateCoachRequest();
        request.setCoachName(eksCoachName + " updated");
        request.setCoachTypeId(Long.toString(coachType.getId()));
        request.setCoachNumber(eksCoachNumber + 10);   

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);        

        mockMvc.perform(
                patch("/api/coaches/" + coach.getId())
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
    void testUpdateCoachBadRole() throws Exception {
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

        UpdateCoachRequest request = new UpdateCoachRequest();
        request.setCoachName(eksCoachName + " updated");
        request.setCoachTypeId(Long.toString(coachType.getId()));
        request.setCoachNumber(eksCoachNumber + 10);   

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
                patch("/api/coaches/" + coach.getId())
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
    void testDeletCoachSuccess() throws Exception {
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
                delete("/api/coaches/" + coach.getId())
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
    void testDeletCoachBadId() throws Exception {
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
                delete("/api/coaches/" + coach.getId() + "a")
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
    void testDeletCoachNotFound() throws Exception {
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
                delete("/api/coaches/111111")
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
    void testDeletCoachInvalidToken() throws Exception {
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
                delete("/api/coaches/" + coach.getId())
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
    void testDeletCoachTokenExpired() throws Exception {
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
                delete("/api/coaches/" + coach.getId())
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
    void testDeletCoachNoToken() throws Exception {
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
                delete("/api/coaches/" + coach.getId())
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
    void testDeletCoachBadRole() throws Exception {
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
                delete("/api/coaches/" + coach.getId())
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
    void testGetAllCoachSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        for (int i = 0; i < 50; i++) {
            CoachEntity coach = new CoachEntity();
            coach.setCoachName(eksCoachName + i);
            coach.setCoachNumber(eksCoachNumber + i);
            coach.setCoachTypeEntity(coachType);
            coach.setIsActive(true);
            coach.setUserEntity(user);
            coachRepository.save(coach);
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
                get("/api/coaches")                        
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
    void testGetAllCoachInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        for (int i = 0; i < 50; i++) {
            CoachEntity coach = new CoachEntity();
            coach.setCoachName(eksCoachName + i);
            coach.setCoachNumber(eksCoachNumber + i);
            coach.setCoachTypeEntity(coachType);
            coach.setIsActive(true);
            coach.setUserEntity(user);
            coachRepository.save(coach);
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
                get("/api/coaches")                        
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
    void testGetAllCoachTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        for (int i = 0; i < 50; i++) {
            CoachEntity coach = new CoachEntity();
            coach.setCoachName(eksCoachName + i);
            coach.setCoachNumber(eksCoachNumber + i);
            coach.setCoachTypeEntity(coachType);
            coach.setIsActive(true);
            coach.setUserEntity(user);
            coachRepository.save(coach);
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
                get("/api/coaches")                        
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
    void testGetAllCoachNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        for (int i = 0; i < 50; i++) {
            CoachEntity coach = new CoachEntity();
            coach.setCoachName(eksCoachName + i);
            coach.setCoachNumber(eksCoachNumber + i);
            coach.setCoachTypeEntity(coachType);
            coach.setIsActive(true);
            coach.setUserEntity(user);
            coachRepository.save(coach);
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
                get("/api/coaches")                        
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
    void testGetAllCoachBadRole() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RoleEntity role = roleRepository.findByName("ROLE_USER").orElse(null);
        
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        for (int i = 0; i < 50; i++) {
            CoachEntity coach = new CoachEntity();
            coach.setCoachName(eksCoachName + i);
            coach.setCoachNumber(eksCoachNumber + i);
            coach.setCoachTypeEntity(coachType);
            coach.setIsActive(true);
            coach.setUserEntity(user);
            coachRepository.save(coach);
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
                get("/api/coaches")                        
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
    
    @Test
    void testSearchCoachByName() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        for (int i = 0; i < 50; i++) {
            CoachEntity coach = new CoachEntity();
            coach.setCoachName(eksCoachName + i);
            coach.setCoachNumber(eksCoachNumber + i);
            coach.setCoachTypeEntity(coachType);
            coach.setIsActive(true);
            coach.setUserEntity(user);
            coachRepository.save(coach);
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
                get("/api/coaches/search")
                        .queryParam("coachName", "Eksekutif")                         
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
    void testSearchCoachNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        for (int i = 0; i < 50; i++) {
            CoachEntity coach = new CoachEntity();
            coach.setCoachName(eksCoachName + i);
            coach.setCoachNumber(eksCoachNumber + i);
            coach.setCoachTypeEntity(coachType);
            coach.setIsActive(true);
            coach.setUserEntity(user);
            coachRepository.save(coach);
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
                get("/api/coaches/search")
                        .queryParam("coachName", "Pan")
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
    void testSearchCoachInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        for (int i = 0; i < 50; i++) {
            CoachEntity coach = new CoachEntity();
            coach.setCoachName(eksCoachName + i);
            coach.setCoachNumber(eksCoachNumber + i);
            coach.setCoachTypeEntity(coachType);
            coach.setIsActive(true);
            coach.setUserEntity(user);
            coachRepository.save(coach);
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
                get("/api/coaches/search")
                        .queryParam("coachName", "Eksekutif")                         
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
    void testSearchCoachByTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        for (int i = 0; i < 50; i++) {
            CoachEntity coach = new CoachEntity();
            coach.setCoachName(eksCoachName + i);
            coach.setCoachNumber(eksCoachNumber + i);
            coach.setCoachTypeEntity(coachType);
            coach.setIsActive(true);
            coach.setUserEntity(user);
            coachRepository.save(coach);
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
                get("/api/coaches/search")
                        .queryParam("coachName", "Eksekutif")                         
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
    void testSearchCoachNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        for (int i = 0; i < 50; i++) {
            CoachEntity coach = new CoachEntity();
            coach.setCoachName(eksCoachName + i);
            coach.setCoachNumber(eksCoachNumber + i);
            coach.setCoachTypeEntity(coachType);
            coach.setIsActive(true);
            coach.setUserEntity(user);
            coachRepository.save(coach);
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
                get("/api/coaches/search")
                        .queryParam("coachName", "Eksekutif")                         
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
    void testSearchCoachByBadRole() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RoleEntity role = roleRepository.findByName("ROLE_NOT_FOUND").orElse(null);
        
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);

        for (int i = 0; i < 50; i++) {
            CoachEntity coach = new CoachEntity();
            coach.setCoachName(eksCoachName + i);
            coach.setCoachNumber(eksCoachNumber + i);
            coach.setCoachTypeEntity(coachType);
            coach.setIsActive(true);
            coach.setUserEntity(user);
            coachRepository.save(coach);
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
                get("/api/coaches/search")
                        .queryParam("coachName", "Eksekutif")                         
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

    @Test
    void testAssignSeatToCoachSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);        

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("11A");
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
                post("/api/coaches/" + coach.getId() + "/seats/" + seat.getId())
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
            assertNotEquals(0, response.getData().getSeats().size());
        });        
    }

    @Test
    void testAssignSeatBadIdToCoach() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);        

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("11A");
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
                post("/api/coaches/" + coach.getId() + "a/seats/" + seat.getId())
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
    void testAssignSeatNotFoundToCoach() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);        

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("11A");
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
                post("/api/coaches/111111/seats/" + seat.getId())
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
    void testAssignSeatToCoachBadId() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);        

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("11A");
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
                post("/api/coaches/" + coach.getId() + "/seats/" + seat.getId() + "a")
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
    void testAssignSeatToCoachNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);        

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("11A");
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
                post("/api/coaches/" + coach.getId() + "/seats/111111")
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
    void testAssignSeatToCoachInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);        

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("11A");
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
                post("/api/coaches/" + coach.getId() + "/seats/" + seat.getId())
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
    void testAssignSeatToCoachTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);        

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("11A");
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
                post("/api/coaches/" + coach.getId() + "/seats/" + seat.getId())
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
    void testAssignSeatToCoachNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);        

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("11A");
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
                post("/api/coaches/" + coach.getId() + "/seats/" + seat.getId())
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
    void testAssignSeatToCoachBadRole() throws Exception {
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

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("11A");
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
                post("/api/coaches/" + coach.getId() + "/seats/" + seat.getId())
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

    @Test
    void testRemoveSeatToCoachSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);        

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("11A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        coach.getSeats().add(seat);
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
                delete("/api/coaches/" + coach.getId() + "/seats/" + seat.getId())
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
            assertEquals(0, response.getData().getSeats().size());
        });        
    }

    @Test
    void testRemoveSeatBadIdToCoach() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);        

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("11A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        coach.getSeats().add(seat);
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
                delete("/api/coaches/" + coach.getId() + "a/seats/" + seat.getId())
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
    void testRemoveSeatNotFoundToCoach() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);        

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("11A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        coach.getSeats().add(seat);
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
                delete("/api/coaches/111111/seats/" + seat.getId())
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
    void testRemoveSeatToCoachBadId() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);        

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("11A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        coach.getSeats().add(seat);
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
                delete("/api/coaches/" + coach.getId() + "/seats/" + seat.getId() + "a")
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
    void testRemoveSeatToCoachNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);        

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("11A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        coach.getSeats().add(seat);
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
                delete("/api/coaches/" + coach.getId() + "/seats/111111")
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
    void testRemoveSeatToCoachInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);        

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("11A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        coach.getSeats().add(seat);
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
                delete("/api/coaches/" + coach.getId() + "/seats/" + seat.getId())
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
    void testRemoveSeatToCoachTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);        

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("11A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        coach.getSeats().add(seat);
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
                delete("/api/coaches/" + coach.getId() + "/seats/" + seat.getId())
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
    void testRemoveSeatToCoachNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName(eksCoachType).orElse(null);        

        CoachEntity coach = new CoachEntity();
        coach.setCoachName(eksCoachName);
        coach.setCoachNumber(eksCoachNumber);
        coach.setCoachTypeEntity(coachType);
        coach.setIsActive(true);
        coach.setUserEntity(user);
        coachRepository.save(coach);

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("11A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        coach.getSeats().add(seat);
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
                delete("/api/coaches/" + coach.getId() + "/seats/" + seat.getId())
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
    void testRemoveSeatToCoachBadRole() throws Exception {
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

        SeatEntity seat = new SeatEntity();
        seat.setSeatNumber("11A");
        seat.setUserEntity(user);
        seatRepository.save(seat);

        coach.getSeats().add(seat);
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
                delete("/api/coaches/" + coach.getId() + "/seats/" + seat.getId())
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
