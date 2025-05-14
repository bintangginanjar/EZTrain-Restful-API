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
import restful.api.eztrain.entity.StationEntity;
import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.model.RegisterStationRequest;
import restful.api.eztrain.model.StationResponse;
import restful.api.eztrain.model.UpdateStationRequest;
import restful.api.eztrain.model.WebResponse;
import restful.api.eztrain.repository.RoleRepository;
import restful.api.eztrain.repository.StationRepository;
import restful.api.eztrain.repository.UserRepository;
import restful.api.eztrain.security.JwtUtil;
import restful.api.eztrain.security.SecurityConstants;

@EnableWebMvc
@SpringBootTest
@AutoConfigureMockMvc
public class StationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private StationRepository stationRepository;

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

    private final String bdCode = "BD";
    private final String bdName = "Bandung";
    private final String bdCity = "Bandung";
    private final String bdProvince = "West Java";

    private final String gbCode = "GBR";
    private final String gbName = "Gambir";
    private final String gbCity = "Jakarta";
    private final String gbProvince = "DKI Jakarta";

    private final String ykCode = "YK";
    private final String ykName = "Yogyakarta";
    private final String ykCity = "Yogyakarta";
    private final String ykProvince = "DI Yogyakarta";    

    @BeforeEach
    void setUp() {                

        stationRepository.deleteAll();
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
    void testRegisterStationSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RegisterStationRequest request = new RegisterStationRequest();
        request.setCode(bdCode);
        request.setName(bdName);
        request.setCity(bdCity);
        request.setProvince(bdProvince);

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
                post("/api/stations")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<StationResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(request.getCode(), response.getData().getCode());
            assertEquals(request.getName(), response.getData().getName());
            assertEquals(request.getCity(), response.getData().getCity());
            assertEquals(request.getProvince(), response.getData().getProvince());
        });
    }

    @Test
    void testRegisterStationBlank() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RegisterStationRequest request = new RegisterStationRequest();
        request.setCode(bdCode);
        request.setName(bdName);
        request.setCity(bdCity);
        request.setProvince("");

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
                post("/api/stations")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<StationResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testRegisterStationDuplicate() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        StationEntity station = new StationEntity();
        station.setCode(bdCode);
        station.setName(bdName);
        station.setCity(bdCity);
        station.setProvince(bdProvince);
        station.setIsActive(true);
        station.setUserEntity(user);
        stationRepository.save(station);

        RegisterStationRequest request = new RegisterStationRequest();
        request.setCode(bdCode);
        request.setName(bdName);
        request.setCity(bdCity);
        request.setProvince(bdProvince);

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
                post("/api/stations")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<StationResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testRegisterStationInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RegisterStationRequest request = new RegisterStationRequest();
        request.setCode(bdCode);
        request.setName(bdName);
        request.setCity(bdCity);
        request.setProvince(bdProvince);

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
                post("/api/stations")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<StationResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testRegisterStationTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RegisterStationRequest request = new RegisterStationRequest();
        request.setCode(bdCode);
        request.setName(bdName);
        request.setCity(bdCity);
        request.setProvince(bdProvince);

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
                post("/api/stations")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<StationResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testRegisterStationNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RegisterStationRequest request = new RegisterStationRequest();
        request.setCode(bdCode);
        request.setName(bdName);
        request.setCity(bdCity);
        request.setProvince(bdProvince);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);

        mockMvc.perform(
                post("/api/stations")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                                              
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<StationResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testRegisterStationBadRole() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RoleEntity role = roleRepository.findByName("ROLE_USER").orElse(null);
        
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);

        RegisterStationRequest request = new RegisterStationRequest();
        request.setCode(bdCode);
        request.setName(bdName);
        request.setCity(bdCity);
        request.setProvince(bdProvince);

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
                post("/api/stations")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<StationResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testGetStationSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        StationEntity station = new StationEntity();
        station.setCode(bdCode);
        station.setName(bdName);
        station.setCity(bdCity);
        station.setProvince(bdProvince);
        station.setIsActive(true);
        station.setUserEntity(user);
        stationRepository.save(station);

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
                get("/api/stations/" + station.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<StationResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(station.getCode(), response.getData().getCode());
            assertEquals(station.getName(), response.getData().getName());
            assertEquals(station.getCity(), response.getData().getCity());
            assertEquals(station.getProvince(), response.getData().getProvince());
        });
    }

    @Test
    void testGetStationBadId() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        StationEntity station = new StationEntity();
        station.setCode(bdCode);
        station.setName(bdName);
        station.setCity(bdCity);
        station.setProvince(bdProvince);
        station.setIsActive(true);
        station.setUserEntity(user);
        stationRepository.save(station);

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
                get("/api/stations/" + station.getId() + "a")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<StationResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testGetStationNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        StationEntity station = new StationEntity();
        station.setCode(bdCode);
        station.setName(bdName);
        station.setCity(bdCity);
        station.setProvince(bdProvince);
        station.setIsActive(true);
        station.setUserEntity(user);
        stationRepository.save(station);

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
                get("/api/stations/111111")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
                WebResponse<StationResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testGetStationInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        StationEntity station = new StationEntity();
        station.setCode(bdCode);
        station.setName(bdName);
        station.setCity(bdCity);
        station.setProvince(bdProvince);
        station.setIsActive(true);
        station.setUserEntity(user);
        stationRepository.save(station);

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
                get("/api/stations/" + station.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<StationResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testGetStationTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        StationEntity station = new StationEntity();
        station.setCode(bdCode);
        station.setName(bdName);
        station.setCity(bdCity);
        station.setProvince(bdProvince);
        station.setIsActive(true);
        station.setUserEntity(user);
        stationRepository.save(station);

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
                get("/api/stations/" + station.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<StationResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testGetStationNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        StationEntity station = new StationEntity();
        station.setCode(bdCode);
        station.setName(bdName);
        station.setCity(bdCity);
        station.setProvince(bdProvince);
        station.setIsActive(true);
        station.setUserEntity(user);
        stationRepository.save(station);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);        

        mockMvc.perform(
                get("/api/stations/" + station.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                                                                   
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<StationResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testGetStationBadRole() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RoleEntity role = roleRepository.findByName("ROLE_USER").orElse(null);
        
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);

        StationEntity station = new StationEntity();
        station.setCode(bdCode);
        station.setName(bdName);
        station.setCity(bdCity);
        station.setProvince(bdProvince);
        station.setIsActive(true);
        station.setUserEntity(user);
        stationRepository.save(station);

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
                get("/api/stations/" + station.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<StationResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testGetAllStationSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        for (int i = 0; i < 50; i++) {
            StationEntity station = new StationEntity();
            station.setCode(bdCode + i);
            station.setName(bdName + i);
            station.setCity(bdCity + i);
            station.setProvince(bdProvince + i);
            station.setIsActive(true);
            station.setUserEntity(user);
            stationRepository.save(station);
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
                get("/api/stations")
                        .queryParam("code", bdCode)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<List<StationResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());            
            assertEquals(10, response.getData().size());
            assertEquals(5, response.getPaging().getTotalPage());
            assertEquals(0, response.getPaging().getCurrentPage());
            assertEquals(10, response.getPaging().getSize());
        });

        mockMvc.perform(
                get("/api/stations")
                        .queryParam("name", bdName)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<List<StationResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());            
            assertEquals(10, response.getData().size());
            assertEquals(5, response.getPaging().getTotalPage());
            assertEquals(0, response.getPaging().getCurrentPage());
            assertEquals(10, response.getPaging().getSize());
        });

        mockMvc.perform(
                get("/api/stations")
                        .queryParam("city", bdCity)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<List<StationResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());            
            assertEquals(10, response.getData().size());
            assertEquals(5, response.getPaging().getTotalPage());
            assertEquals(0, response.getPaging().getCurrentPage());
            assertEquals(10, response.getPaging().getSize());
        });

        mockMvc.perform(
                get("/api/stations")
                        .queryParam("code", bdProvince)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<List<StationResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());            
            assertEquals(10, response.getData().size());
            assertEquals(5, response.getPaging().getTotalPage());
            assertEquals(0, response.getPaging().getCurrentPage());
            assertEquals(10, response.getPaging().getSize());
        });
    }

    @Test
    void testGetAllStationNotFound() throws Exception {
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
                get("/api/stations")
                        .queryParam("code", bdCode)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<List<StationResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());            
            assertEquals(0, response.getData().size());
            assertEquals(0, response.getPaging().getTotalPage());
            assertEquals(0, response.getPaging().getCurrentPage());
            assertEquals(10, response.getPaging().getSize());
        });
    }

    @Test
    void testGetAllStationInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        for (int i = 0; i < 50; i++) {
            StationEntity station = new StationEntity();
            station.setCode(bdCode + i);
            station.setName(bdName + i);
            station.setCity(bdCity + i);
            station.setProvince(bdProvince + i);
            station.setIsActive(true);
            station.setUserEntity(user);
            stationRepository.save(station);
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
                get("/api/stations")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<List<StationResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testGetAllStationTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        for (int i = 0; i < 50; i++) {
            StationEntity station = new StationEntity();
            station.setCode(bdCode + i);
            station.setName(bdName + i);
            station.setCity(bdCity + i);
            station.setProvince(bdProvince + i);
            station.setIsActive(true);
            station.setUserEntity(user);
            stationRepository.save(station);
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
                get("/api/stations")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<List<StationResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testGetAllStationNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        for (int i = 0; i < 50; i++) {
            StationEntity station = new StationEntity();
            station.setCode(bdCode + i);
            station.setName(bdName + i);
            station.setCity(bdCity + i);
            station.setProvince(bdProvince + i);
            station.setIsActive(true);
            station.setUserEntity(user);
            stationRepository.save(station);
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
                get("/api/stations")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                                                
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<List<StationResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testGetAllStationBadRole() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RoleEntity role = roleRepository.findByName("ROLE_NOT_FOUND").orElse(null);
        
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);

        for (int i = 0; i < 50; i++) {
            StationEntity station = new StationEntity();
            station.setCode(bdCode + i);
            station.setName(bdName + i);
            station.setCity(bdCity + i);
            station.setProvince(bdProvince + i);
            station.setIsActive(true);
            station.setUserEntity(user);
            stationRepository.save(station);
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
                get("/api/stations")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<List<StationResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testUpdateStationSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        StationEntity bdStation = new StationEntity();
        bdStation.setCode(bdCode);
        bdStation.setName(bdName);
        bdStation.setCity(bdCity);
        bdStation.setProvince(bdProvince);
        bdStation.setIsActive(true);
        bdStation.setUserEntity(user);
        stationRepository.save(bdStation);
        
        StationEntity gbStation = new StationEntity();
        gbStation.setCode(gbCode);
        gbStation.setName(gbName);
        gbStation.setCity(gbCity);
        gbStation.setProvince(gbProvince);
        gbStation.setIsActive(true);
        gbStation.setUserEntity(user);
        stationRepository.save(gbStation);

        StationEntity ykStation = new StationEntity();
        ykStation.setCode(ykCode);
        ykStation.setName(ykName);
        ykStation.setCity(ykCity);
        ykStation.setProvince(ykProvince);
        ykStation.setIsActive(true);
        ykStation.setUserEntity(user);
        stationRepository.save(ykStation);

        UpdateStationRequest request = new UpdateStationRequest();        
        request.setCode(bdCode + "updated");
        request.setName(bdName + "updated");
        request.setCity(bdCity + "updated");
        request.setProvince(bdProvince + "updated");

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
                patch("/api/stations/" + bdStation.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<StationResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(request.getCode(), response.getData().getCode());
            assertEquals(request.getName(), response.getData().getName());
            assertEquals(request.getCity(), response.getData().getCity());
            assertEquals(request.getProvince(), response.getData().getProvince());
        });
    }

    @Test
    void testUpdateStationDuplicate() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        StationEntity bdStation = new StationEntity();
        bdStation.setCode(bdCode);
        bdStation.setName(bdName);
        bdStation.setCity(bdCity);
        bdStation.setProvince(bdProvince);
        bdStation.setIsActive(true);
        bdStation.setUserEntity(user);
        stationRepository.save(bdStation);
        
        StationEntity gbStation = new StationEntity();
        gbStation.setCode(gbCode);
        gbStation.setName(gbName);
        gbStation.setCity(gbCity);
        gbStation.setProvince(gbProvince);
        gbStation.setIsActive(true);
        gbStation.setUserEntity(user);
        stationRepository.save(gbStation);

        StationEntity ykStation = new StationEntity();
        ykStation.setCode(ykCode);
        ykStation.setName(ykName);
        ykStation.setCity(ykCity);
        ykStation.setProvince(ykProvince);
        ykStation.setIsActive(true);
        ykStation.setUserEntity(user);
        stationRepository.save(ykStation);

        UpdateStationRequest request = new UpdateStationRequest();        
        request.setCode(ykCode);
        request.setName(bdName + "updated");
        request.setCity(bdCity + "updated");
        request.setProvince(bdProvince + "updated");

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
                patch("/api/stations/" + bdStation.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<StationResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testUpdateStationInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        StationEntity bdStation = new StationEntity();
        bdStation.setCode(bdCode);
        bdStation.setName(bdName);
        bdStation.setCity(bdCity);
        bdStation.setProvince(bdProvince);
        bdStation.setIsActive(true);
        bdStation.setUserEntity(user);
        stationRepository.save(bdStation);
        
        StationEntity gbStation = new StationEntity();
        gbStation.setCode(gbCode);
        gbStation.setName(gbName);
        gbStation.setCity(gbCity);
        gbStation.setProvince(gbProvince);
        gbStation.setIsActive(true);
        gbStation.setUserEntity(user);
        stationRepository.save(gbStation);

        StationEntity ykStation = new StationEntity();
        ykStation.setCode(ykCode);
        ykStation.setName(ykName);
        ykStation.setCity(ykCity);
        ykStation.setProvince(ykProvince);
        ykStation.setIsActive(true);
        ykStation.setUserEntity(user);
        stationRepository.save(ykStation);

        UpdateStationRequest request = new UpdateStationRequest();        
        request.setCode(bdCode + "updated");
        request.setName(bdName + "updated");
        request.setCity(bdCity + "updated");
        request.setProvince(bdProvince + "updated");

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
                patch("/api/stations/" + bdStation.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<StationResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testUpdateStationTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        StationEntity bdStation = new StationEntity();
        bdStation.setCode(bdCode);
        bdStation.setName(bdName);
        bdStation.setCity(bdCity);
        bdStation.setProvince(bdProvince);
        bdStation.setIsActive(true);
        bdStation.setUserEntity(user);
        stationRepository.save(bdStation);
        
        StationEntity gbStation = new StationEntity();
        gbStation.setCode(gbCode);
        gbStation.setName(gbName);
        gbStation.setCity(gbCity);
        gbStation.setProvince(gbProvince);
        gbStation.setIsActive(true);
        gbStation.setUserEntity(user);
        stationRepository.save(gbStation);

        StationEntity ykStation = new StationEntity();
        ykStation.setCode(ykCode);
        ykStation.setName(ykName);
        ykStation.setCity(ykCity);
        ykStation.setProvince(ykProvince);
        ykStation.setIsActive(true);
        ykStation.setUserEntity(user);
        stationRepository.save(ykStation);

        UpdateStationRequest request = new UpdateStationRequest();        
        request.setCode(bdCode + "updated");
        request.setName(bdName + "updated");
        request.setCity(bdCity + "updated");
        request.setProvince(bdProvince + "updated");

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
                patch("/api/stations/" + bdStation.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<StationResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testUpdateStationNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        StationEntity bdStation = new StationEntity();
        bdStation.setCode(bdCode);
        bdStation.setName(bdName);
        bdStation.setCity(bdCity);
        bdStation.setProvince(bdProvince);
        bdStation.setIsActive(true);
        bdStation.setUserEntity(user);
        stationRepository.save(bdStation);
        
        StationEntity gbStation = new StationEntity();
        gbStation.setCode(gbCode);
        gbStation.setName(gbName);
        gbStation.setCity(gbCity);
        gbStation.setProvince(gbProvince);
        gbStation.setIsActive(true);
        gbStation.setUserEntity(user);
        stationRepository.save(gbStation);

        StationEntity ykStation = new StationEntity();
        ykStation.setCode(ykCode);
        ykStation.setName(ykName);
        ykStation.setCity(ykCity);
        ykStation.setProvince(ykProvince);
        ykStation.setIsActive(true);
        ykStation.setUserEntity(user);
        stationRepository.save(ykStation);

        UpdateStationRequest request = new UpdateStationRequest();        
        request.setCode(bdCode + "updated");
        request.setName(bdName + "updated");
        request.setCity(bdCity + "updated");
        request.setProvince(bdProvince + "updated");

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);        

        mockMvc.perform(
                patch("/api/stations/" + bdStation.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                                              
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<StationResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testUpdateStationBadRole() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RoleEntity role = roleRepository.findByName("ROLE_USER").orElse(null);
        
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);

        StationEntity bdStation = new StationEntity();
        bdStation.setCode(bdCode);
        bdStation.setName(bdName);
        bdStation.setCity(bdCity);
        bdStation.setProvince(bdProvince);
        bdStation.setIsActive(true);
        bdStation.setUserEntity(user);
        stationRepository.save(bdStation);
        
        StationEntity gbStation = new StationEntity();
        gbStation.setCode(gbCode);
        gbStation.setName(gbName);
        gbStation.setCity(gbCity);
        gbStation.setProvince(gbProvince);
        gbStation.setIsActive(true);
        gbStation.setUserEntity(user);
        stationRepository.save(gbStation);

        StationEntity ykStation = new StationEntity();
        ykStation.setCode(ykCode);
        ykStation.setName(ykName);
        ykStation.setCity(ykCity);
        ykStation.setProvince(ykProvince);
        ykStation.setIsActive(true);
        ykStation.setUserEntity(user);
        stationRepository.save(ykStation);

        UpdateStationRequest request = new UpdateStationRequest();        
        request.setCode(bdCode + "updated");
        request.setName(bdName + "updated");
        request.setCity(bdCity + "updated");
        request.setProvince(bdProvince + "updated");

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
                patch("/api/stations/" + bdStation.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<StationResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testDeleteStationSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        StationEntity station = new StationEntity();
        station.setCode(bdCode);
        station.setName(bdName);
        station.setCity(bdCity);
        station.setProvince(bdProvince);
        station.setIsActive(true);
        station.setUserEntity(user);
        stationRepository.save(station);

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
                delete("/api/stations/" + station.getId())
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
    void testDeleteStationBadId() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        StationEntity station = new StationEntity();
        station.setCode(bdCode);
        station.setName(bdName);
        station.setCity(bdCity);
        station.setProvince(bdProvince);
        station.setIsActive(true);
        station.setUserEntity(user);
        stationRepository.save(station);

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
                delete("/api/stations/" + station.getId() + "a")
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
    void testDeleteStationNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        StationEntity station = new StationEntity();
        station.setCode(bdCode);
        station.setName(bdName);
        station.setCity(bdCity);
        station.setProvince(bdProvince);
        station.setIsActive(true);
        station.setUserEntity(user);
        stationRepository.save(station);

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
                delete("/api/stations/1111111")
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
    void testDeleteStationInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        StationEntity station = new StationEntity();
        station.setCode(bdCode);
        station.setName(bdName);
        station.setCity(bdCity);
        station.setProvince(bdProvince);
        station.setIsActive(true);
        station.setUserEntity(user);
        stationRepository.save(station);

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
                delete("/api/stations/" + station.getId())
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
    void testDeleteStationTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        StationEntity station = new StationEntity();
        station.setCode(bdCode);
        station.setName(bdName);
        station.setCity(bdCity);
        station.setProvince(bdProvince);
        station.setIsActive(true);
        station.setUserEntity(user);
        stationRepository.save(station);

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
                delete("/api/stations/" + station.getId())
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
    void testDeleteStationNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        StationEntity station = new StationEntity();
        station.setCode(bdCode);
        station.setName(bdName);
        station.setCity(bdCity);
        station.setProvince(bdProvince);
        station.setIsActive(true);
        station.setUserEntity(user);
        stationRepository.save(station);

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + SecurityConstants.JWTexpiration);
        userRepository.save(user);        

        mockMvc.perform(
                delete("/api/stations/" + station.getId())
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
    void testDeleteStationBadRole() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RoleEntity role = roleRepository.findByName("ROLE_USER").orElse(null);
        
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);

        StationEntity station = new StationEntity();
        station.setCode(bdCode);
        station.setName(bdName);
        station.setCity(bdCity);
        station.setProvince(bdProvince);
        station.setIsActive(true);
        station.setUserEntity(user);
        stationRepository.save(station);

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
                delete("/api/stations/" + station.getId())
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
}
