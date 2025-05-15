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
import restful.api.eztrain.entity.TrainEntity;
import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.model.RegisterTrainRequest;
import restful.api.eztrain.model.TrainResponse;
import restful.api.eztrain.model.UpdateTrainRequest;
import restful.api.eztrain.model.WebResponse;
import restful.api.eztrain.repository.RoleRepository;
import restful.api.eztrain.repository.TrainRepository;
import restful.api.eztrain.repository.UserRepository;
import restful.api.eztrain.security.JwtUtil;
import restful.api.eztrain.security.SecurityConstants;

@EnableWebMvc
@SpringBootTest
@AutoConfigureMockMvc
public class TrainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TrainRepository trainRepository;

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

    private final String trainName = "Argo Parahyangan";
    private final String trainType = "Argo";
    private final String trainOperator = "KAI";

    @BeforeEach
    void setUp() {                

        trainRepository.deleteAll();
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
    void testRegisterTrainSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RegisterTrainRequest request = new RegisterTrainRequest();        
        request.setName(trainName);
        request.setTrainType(trainType);
        request.setOperator(trainOperator);        

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
                post("/api/trains")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<TrainResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(request.getName(), response.getData().getName());            
            assertEquals(request.getTrainType(), response.getData().getTrainType());
            assertEquals(request.getOperator(), response.getData().getOperator());
        });
    }

    @Test
    void testRegisterTrainBlank() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RegisterTrainRequest request = new RegisterTrainRequest();        
        request.setName(trainName);
        request.setTrainType(trainType);
        request.setOperator("");        

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
                post("/api/trains")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<TrainResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testRegisterTrainDuplicate() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        TrainEntity train = new TrainEntity();
        train.setName(trainName);
        train.setTrainType(trainType);
        train.setOperator(trainOperator);
        train.setIsActive(true);
        train.setUserEntity(user);
        trainRepository.save(train);

        RegisterTrainRequest request = new RegisterTrainRequest();        
        request.setName(trainName);
        request.setTrainType(trainType);
        request.setOperator(trainOperator);        

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
                post("/api/trains")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<TrainResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testRegisterTrainInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RegisterTrainRequest request = new RegisterTrainRequest();        
        request.setName(trainName);
        request.setTrainType(trainType);
        request.setOperator(trainOperator);        

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
                post("/api/trains")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<TrainResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testRegisterTrainTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RegisterTrainRequest request = new RegisterTrainRequest();        
        request.setName(trainName);
        request.setTrainType(trainType);
        request.setOperator(trainOperator);        

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() - SecurityConstants.JWTexpiration);
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                post("/api/trains")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<TrainResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testRegisterTrainNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RegisterTrainRequest request = new RegisterTrainRequest();        
        request.setName(trainName);
        request.setTrainType(trainType);
        request.setOperator(trainOperator);        

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);        

        mockMvc.perform(
                post("/api/trains")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                                            
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<TrainResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testRegisterTrainBadRole() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RoleEntity role = roleRepository.findByName("ROLE_NOT_FOUND").orElse(null);
        
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);

        RegisterTrainRequest request = new RegisterTrainRequest();        
        request.setName(trainName);
        request.setTrainType(trainType);
        request.setOperator(trainOperator);        

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
                post("/api/trains")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<TrainResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testGetTrainSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        TrainEntity train = new TrainEntity();
        train.setName(trainName);
        train.setTrainType(trainType);
        train.setOperator(trainOperator);
        train.setIsActive(true);
        train.setUserEntity(user);
        trainRepository.save(train);       

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
                get("/api/trains/" + train.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<TrainResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(train.getId(), response.getData().getId());
            assertEquals(train.getName(), response.getData().getName());            
            assertEquals(train.getTrainType(), response.getData().getTrainType());
            assertEquals(train.getOperator(), response.getData().getOperator());
        });
    }

    @Test
    void testGetTrainBadId() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        TrainEntity train = new TrainEntity();
        train.setName(trainName);
        train.setTrainType(trainType);
        train.setOperator(trainOperator);
        train.setIsActive(true);
        train.setUserEntity(user);
        trainRepository.save(train);       

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
                get("/api/trains/" + train.getId() + "a")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<TrainResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testGetTrainNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        TrainEntity train = new TrainEntity();
        train.setName(trainName);
        train.setTrainType(trainType);
        train.setOperator(trainOperator);
        train.setIsActive(true);
        train.setUserEntity(user);
        trainRepository.save(train);       

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
                get("/api/trains/111111")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
                WebResponse<TrainResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testGetTrainInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        TrainEntity train = new TrainEntity();
        train.setName(trainName);
        train.setTrainType(trainType);
        train.setOperator(trainOperator);
        train.setIsActive(true);
        train.setUserEntity(user);
        trainRepository.save(train);       

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
                get("/api/trains/" + train.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<TrainResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testGetTrainTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        TrainEntity train = new TrainEntity();
        train.setName(trainName);
        train.setTrainType(trainType);
        train.setOperator(trainOperator);
        train.setIsActive(true);
        train.setUserEntity(user);
        trainRepository.save(train);       

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() - SecurityConstants.JWTexpiration);
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                get("/api/trains/" + train.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<TrainResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testGetTrainNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        TrainEntity train = new TrainEntity();
        train.setName(trainName);
        train.setTrainType(trainType);
        train.setOperator(trainOperator);
        train.setIsActive(true);
        train.setUserEntity(user);
        trainRepository.save(train);       

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);

        mockMvc.perform(
                get("/api/trains/" + train.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                                                                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<TrainResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testGetTrainBadRole() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RoleEntity role = roleRepository.findByName("ROLE_NOT_FOUND").orElse(null);
        
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);

        TrainEntity train = new TrainEntity();
        train.setName(trainName);
        train.setTrainType(trainType);
        train.setOperator(trainOperator);
        train.setIsActive(true);
        train.setUserEntity(user);
        trainRepository.save(train);       

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
                get("/api/trains/" + train.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<TrainResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testUpdateTrainSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        TrainEntity train = new TrainEntity();
        train.setName(trainName);
        train.setTrainType(trainType);
        train.setOperator(trainOperator);
        train.setIsActive(true);
        train.setUserEntity(user);
        trainRepository.save(train);

        UpdateTrainRequest request = new UpdateTrainRequest();
        request.setName(trainName + "updated");
        request.setTrainType(trainType + "updated");
        request.setOperator(trainOperator + "updated");
        request.setIsActive(false);

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
                patch("/api/trains/" + train.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<TrainResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(train.getId(), response.getData().getId());
            assertEquals(request.getName(), response.getData().getName());            
            assertEquals(request.getTrainType(), response.getData().getTrainType());
            assertEquals(request.getOperator(), response.getData().getOperator());
            assertEquals(request.getIsActive(), response.getData().getIsActive());
        });
    }

    @Test
    void testUpdateTrainDuplicate() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        TrainEntity train = new TrainEntity();
        train.setName(trainName);
        train.setTrainType(trainType);
        train.setOperator(trainOperator);
        train.setIsActive(true);
        train.setUserEntity(user);
        trainRepository.save(train);

        TrainEntity argoTrain = new TrainEntity();
        argoTrain.setName("Argo Lawu");
        argoTrain.setTrainType("Argo");
        argoTrain.setOperator(trainOperator);
        argoTrain.setIsActive(true);
        argoTrain.setUserEntity(user);
        trainRepository.save(argoTrain);

        UpdateTrainRequest request = new UpdateTrainRequest();
        request.setName("Argo Lawu");
        request.setTrainType(trainType);
        request.setOperator(trainOperator);
        request.setIsActive(true);

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
                patch("/api/trains/" + train.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<TrainResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }    

    @Test
    void testUpdateTrainBadId() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        TrainEntity train = new TrainEntity();
        train.setName(trainName);
        train.setTrainType(trainType);
        train.setOperator(trainOperator);
        train.setIsActive(true);
        train.setUserEntity(user);
        trainRepository.save(train);

        UpdateTrainRequest request = new UpdateTrainRequest();
        request.setName(trainName + "updated");
        request.setTrainType(trainType + "updated");
        request.setOperator(trainOperator + "updated");
        request.setIsActive(false);

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
                patch("/api/trains/" + train.getId() + "a")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<TrainResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testUpdateTrainNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        TrainEntity train = new TrainEntity();
        train.setName(trainName);
        train.setTrainType(trainType);
        train.setOperator(trainOperator);
        train.setIsActive(true);
        train.setUserEntity(user);
        trainRepository.save(train);

        UpdateTrainRequest request = new UpdateTrainRequest();
        request.setName(trainName + "updated");
        request.setTrainType(trainType + "updated");
        request.setOperator(trainOperator + "updated");
        request.setIsActive(false);

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
                patch("/api/trains/9999999")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
                WebResponse<TrainResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testUpdateTrainInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        TrainEntity train = new TrainEntity();
        train.setName(trainName);
        train.setTrainType(trainType);
        train.setOperator(trainOperator);
        train.setIsActive(true);
        train.setUserEntity(user);
        trainRepository.save(train);

        UpdateTrainRequest request = new UpdateTrainRequest();
        request.setName(trainName + "updated");
        request.setTrainType(trainType + "updated");
        request.setOperator(trainOperator + "updated");
        request.setIsActive(false);

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
                patch("/api/trains/" + train.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<TrainResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testUpdateTrainTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        TrainEntity train = new TrainEntity();
        train.setName(trainName);
        train.setTrainType(trainType);
        train.setOperator(trainOperator);
        train.setIsActive(true);
        train.setUserEntity(user);
        trainRepository.save(train);

        UpdateTrainRequest request = new UpdateTrainRequest();
        request.setName(trainName + "updated");
        request.setTrainType(trainType + "updated");
        request.setOperator(trainOperator + "updated");
        request.setIsActive(false);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() - SecurityConstants.JWTexpiration);
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                patch("/api/trains/" + train.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<TrainResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testUpdateTrainNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        TrainEntity train = new TrainEntity();
        train.setName(trainName);
        train.setTrainType(trainType);
        train.setOperator(trainOperator);
        train.setIsActive(true);
        train.setUserEntity(user);
        trainRepository.save(train);

        UpdateTrainRequest request = new UpdateTrainRequest();
        request.setName(trainName + "updated");
        request.setTrainType(trainType + "updated");
        request.setOperator(trainOperator + "updated");
        request.setIsActive(false);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);

        mockMvc.perform(
                patch("/api/trains/" + train.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                                              
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<TrainResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testUpdateTrainBadRole() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RoleEntity role = roleRepository.findByName("ROLE_NOT_FOUND").orElse(null);
        
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);

        TrainEntity train = new TrainEntity();
        train.setName(trainName);
        train.setTrainType(trainType);
        train.setOperator(trainOperator);
        train.setIsActive(true);
        train.setUserEntity(user);
        trainRepository.save(train);

        UpdateTrainRequest request = new UpdateTrainRequest();
        request.setName(trainName + "updated");
        request.setTrainType(trainType + "updated");
        request.setOperator(trainOperator + "updated");
        request.setIsActive(false);

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
                patch("/api/trains/" + train.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<TrainResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testDeleteTrainSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        TrainEntity train = new TrainEntity();
        train.setName(trainName);
        train.setTrainType(trainType);
        train.setOperator(trainOperator);
        train.setIsActive(true);
        train.setUserEntity(user);
        trainRepository.save(train);       

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
                delete("/api/trains/" + train.getId())
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
    void testDeleteTrainBadId() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        TrainEntity train = new TrainEntity();
        train.setName(trainName);
        train.setTrainType(trainType);
        train.setOperator(trainOperator);
        train.setIsActive(true);
        train.setUserEntity(user);
        trainRepository.save(train);       

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
                delete("/api/trains/" + train.getId() + "a")
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
    void testDeleteTrainNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        TrainEntity train = new TrainEntity();
        train.setName(trainName);
        train.setTrainType(trainType);
        train.setOperator(trainOperator);
        train.setIsActive(true);
        train.setUserEntity(user);
        trainRepository.save(train);       

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
                delete("/api/trains/111111")
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
    void testDeleteTrainInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        TrainEntity train = new TrainEntity();
        train.setName(trainName);
        train.setTrainType(trainType);
        train.setOperator(trainOperator);
        train.setIsActive(true);
        train.setUserEntity(user);
        trainRepository.save(train);       

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
                delete("/api/trains/" + train.getId())
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
    void testDeleteTrainTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        TrainEntity train = new TrainEntity();
        train.setName(trainName);
        train.setTrainType(trainType);
        train.setOperator(trainOperator);
        train.setIsActive(true);
        train.setUserEntity(user);
        trainRepository.save(train);       

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() - SecurityConstants.JWTexpiration);
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                delete("/api/trains/" + train.getId())
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
    void testDeleteTrainNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        TrainEntity train = new TrainEntity();
        train.setName(trainName);
        train.setTrainType(trainType);
        train.setOperator(trainOperator);
        train.setIsActive(true);
        train.setUserEntity(user);
        trainRepository.save(train);       

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);

        mockMvc.perform(
                delete("/api/trains/" + train.getId())
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
    void testDeleteTrainBadRole() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RoleEntity role = roleRepository.findByName("ROLE_NOT_FOUND").orElse(null);
        
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);

        TrainEntity train = new TrainEntity();
        train.setName(trainName);
        train.setTrainType(trainType);
        train.setOperator(trainOperator);
        train.setIsActive(true);
        train.setUserEntity(user);
        trainRepository.save(train);       

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
                delete("/api/trains/" + train.getId())
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
    void testGetAllTrainSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        for (int i = 0; i < 50; i++) {
            TrainEntity train = new TrainEntity();
            train.setName(trainName + i);
            train.setTrainType(trainType + i);
            train.setOperator(trainOperator + i);
            train.setIsActive(true);
            train.setUserEntity(user);
            trainRepository.save(train);
        }

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
                get("/api/trains")                        
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<List<TrainResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(10, response.getData().size());
            assertEquals(5, response.getPaging().getTotalPage());
            assertEquals(0, response.getPaging().getCurrentPage());
            assertEquals(10, response.getPaging().getSize());
        });        
    }    

    @Test
    void testGetAllTrainInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        for (int i = 0; i < 50; i++) {
            TrainEntity train = new TrainEntity();
            train.setName(trainName + i);
            train.setTrainType(trainType + i);
            train.setOperator(trainOperator + i);
            train.setIsActive(true);
            train.setUserEntity(user);
            trainRepository.save(train);
        }

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
                get("/api/trains")                        
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<List<TrainResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });

    }

    @Test
    void testGetAllTrainTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        for (int i = 0; i < 50; i++) {
            TrainEntity train = new TrainEntity();
            train.setName(trainName + i);
            train.setTrainType(trainType + i);
            train.setOperator(trainOperator + i);
            train.setIsActive(true);
            train.setUserEntity(user);
            trainRepository.save(train);
        }

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() - SecurityConstants.JWTexpiration);
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                get("/api/trains")                        
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<List<TrainResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });        
    }

    @Test
    void testGetAllTrainNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        for (int i = 0; i < 50; i++) {
            TrainEntity train = new TrainEntity();
            train.setName(trainName + i);
            train.setTrainType(trainType + i);
            train.setOperator(trainOperator + i);
            train.setIsActive(true);
            train.setUserEntity(user);
            trainRepository.save(train);
        }

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);

        mockMvc.perform(
                get("/api/trains")                        
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                                             
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<List<TrainResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });        
    }

    @Test
    void testGetAllTrainBadRole() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RoleEntity role = roleRepository.findByName("ROLE_NOT_FOUND").orElse(null);
        
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);

        for (int i = 0; i < 50; i++) {
            TrainEntity train = new TrainEntity();
            train.setName(trainName + i);
            train.setTrainType(trainType + i);
            train.setOperator(trainOperator + i);
            train.setIsActive(true);
            train.setUserEntity(user);
            trainRepository.save(train);
        }

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
                get("/api/trains")                        
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<List<TrainResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });        
    }

    @Test
    void testSearchTrainNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        for (int i = 0; i < 50; i++) {
            TrainEntity train = new TrainEntity();
            train.setName(trainName + i);
            train.setTrainType(trainType + i);
            train.setOperator(trainOperator + i);
            train.setIsActive(true);
            train.setUserEntity(user);
            trainRepository.save(train);
        }

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
                get("/api/trains/search")
                        .queryParam("name", "Ekonomi")                        
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<List<TrainResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(0, response.getData().size());
            assertEquals(0, response.getPaging().getTotalPage());
            assertEquals(0, response.getPaging().getCurrentPage());
            assertEquals(10, response.getPaging().getSize());
        });        
    }
    
    @Test
    void testSearchTrainByName() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        for (int i = 0; i < 50; i++) {
            TrainEntity train = new TrainEntity();
            train.setName(trainName + i);
            train.setTrainType(trainType + i);
            train.setOperator(trainOperator + i);
            train.setIsActive(true);
            train.setUserEntity(user);
            trainRepository.save(train);
        }

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
                get("/api/trains/search")
                        .queryParam("name", "Argo")                        
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<List<TrainResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(10, response.getData().size());
            assertEquals(5, response.getPaging().getTotalPage());
            assertEquals(0, response.getPaging().getCurrentPage());
            assertEquals(10, response.getPaging().getSize());
        });        
    }
    
    @Test
    void testSearchTrainByType() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        for (int i = 0; i < 50; i++) {
            TrainEntity train = new TrainEntity();
            train.setName(trainName + i);
            train.setTrainType(trainType + i);
            train.setOperator(trainOperator + i);
            train.setIsActive(true);
            train.setUserEntity(user);
            trainRepository.save(train);
        }

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
                get("/api/trains/search")
                        .queryParam("trainType", "Argo")                        
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<List<TrainResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(10, response.getData().size());
            assertEquals(5, response.getPaging().getTotalPage());
            assertEquals(0, response.getPaging().getCurrentPage());
            assertEquals(10, response.getPaging().getSize());
        });        
    }
    
    @Test
    void testSearchTrainByOperator() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        for (int i = 0; i < 50; i++) {
            TrainEntity train = new TrainEntity();
            train.setName(trainName + i);
            train.setTrainType(trainType + i);
            train.setOperator(trainOperator + i);
            train.setIsActive(true);
            train.setUserEntity(user);
            trainRepository.save(train);
        }

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
                get("/api/trains/search")
                        .queryParam("operator", "KAI")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<List<TrainResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(10, response.getData().size());
            assertEquals(5, response.getPaging().getTotalPage());
            assertEquals(0, response.getPaging().getCurrentPage());
            assertEquals(10, response.getPaging().getSize());
        });        
    }

    @Test
    void testSearchTrainSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        for (int i = 0; i < 50; i++) {
            TrainEntity train = new TrainEntity();
            train.setName(trainName + i);
            train.setTrainType(trainType + i);
            train.setOperator(trainOperator + i);
            train.setIsActive(true);
            train.setUserEntity(user);
            trainRepository.save(train);
        }

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
                get("/api/trains/search")
                        .queryParam("name", "Argo")                        
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<List<TrainResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(10, response.getData().size());
            assertEquals(5, response.getPaging().getTotalPage());
            assertEquals(0, response.getPaging().getCurrentPage());
            assertEquals(10, response.getPaging().getSize());
        });   

        mockMvc.perform(
                get("/api/trains/search")
                        .queryParam("trainType", "Argo")                        
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<List<TrainResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(10, response.getData().size());
            assertEquals(5, response.getPaging().getTotalPage());
            assertEquals(0, response.getPaging().getCurrentPage());
            assertEquals(10, response.getPaging().getSize());
        }); 

        mockMvc.perform(
                get("/api/trains/search")
                        .queryParam("operator", "KAI")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<List<TrainResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(10, response.getData().size());
            assertEquals(5, response.getPaging().getTotalPage());
            assertEquals(0, response.getPaging().getCurrentPage());
            assertEquals(10, response.getPaging().getSize());
        });        
    }

    @Test
    void testSearchTrainInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        for (int i = 0; i < 50; i++) {
            TrainEntity train = new TrainEntity();
            train.setName(trainName + i);
            train.setTrainType(trainType + i);
            train.setOperator(trainOperator + i);
            train.setIsActive(true);
            train.setUserEntity(user);
            trainRepository.save(train);
        }

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
                get("/api/trains/search")
                        .queryParam("name", "Argo")                        
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<List<TrainResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });        
    }

    @Test
    void testSearchTrainTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        for (int i = 0; i < 50; i++) {
            TrainEntity train = new TrainEntity();
            train.setName(trainName + i);
            train.setTrainType(trainType + i);
            train.setOperator(trainOperator + i);
            train.setIsActive(true);
            train.setUserEntity(user);
            trainRepository.save(train);
        }

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() - SecurityConstants.JWTexpiration);
        userRepository.save(user);

        String mockBearerToken = "Bearer " + mockToken;

        mockMvc.perform(
                get("/api/trains/search")
                        .queryParam("name", "Argo")                        
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<List<TrainResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });        
    }

    @Test
    void testSearchTrainNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        for (int i = 0; i < 50; i++) {
            TrainEntity train = new TrainEntity();
            train.setName(trainName + i);
            train.setTrainType(trainType + i);
            train.setOperator(trainOperator + i);
            train.setIsActive(true);
            train.setUserEntity(user);
            trainRepository.save(train);
        }

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);

        mockMvc.perform(
                get("/api/trains/search")
                        .queryParam("name", "Argo")                        
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                                                                    
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<List<TrainResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });        
    }

    @Test
    void testSearchTrainBadRole() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RoleEntity role = roleRepository.findByName("ROLE_NOT_FOUND").orElse(null);
        
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);

        for (int i = 0; i < 50; i++) {
            TrainEntity train = new TrainEntity();
            train.setName(trainName + i);
            train.setTrainType(trainType + i);
            train.setOperator(trainOperator + i);
            train.setIsActive(true);
            train.setUserEntity(user);
            trainRepository.save(train);
        }

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
                get("/api/trains/search")
                        .queryParam("name", "Argo")                        
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<List<TrainResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });        
    }
}
