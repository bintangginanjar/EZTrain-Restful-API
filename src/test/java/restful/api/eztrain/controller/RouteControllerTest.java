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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import restful.api.eztrain.entity.RoleEntity;
import restful.api.eztrain.entity.RouteEntity;
import restful.api.eztrain.entity.StationEntity;
import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.model.RegisterRouteRequest;
import restful.api.eztrain.model.RouteResponse;
import restful.api.eztrain.model.UpdateRouteRequest;
import restful.api.eztrain.model.WebResponse;
import restful.api.eztrain.repository.RoleRepository;
import restful.api.eztrain.repository.RouteRepository;
import restful.api.eztrain.repository.StationRepository;
import restful.api.eztrain.repository.UserRepository;
import restful.api.eztrain.security.JwtUtil;
import restful.api.eztrain.security.SecurityConstants;
import restful.api.eztrain.seeder.RouteSeeder;

@EnableWebMvc
@SpringBootTest
@AutoConfigureMockMvc
public class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SecurityConstants securityConstants;

    @Autowired
    private RouteSeeder routeSeeder;

    @Autowired
    private ObjectMapper objectMapper;

    private final String email = "admin@gmail.com";
    private final String password = "rahasia";

    @BeforeEach
    void setUp() {       
        
        routeRepository.deleteAll();
        
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RoleEntity role = roleRepository.findByName("ROLE_ADMIN").orElse(null);
        
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);

        try {
            routeSeeder.run();
        } catch (Exception e) {            
            e.printStackTrace();
        }
    }

    @Test
    void testRegisterRouteSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        

        StationEntity origin = stationRepository.findByCode("BD").orElse(null);
        StationEntity destination = stationRepository.findByCode("GMR").orElse(null);

        RegisterRouteRequest request = new RegisterRouteRequest();        
        request.setOriginId(origin.getId());
        request.setDestId(destination.getId());
        request.setTripDistance(180.0);
        request.setTripDuration(3.0);        

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
                post("/api/routes")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<RouteResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(request.getOriginId(), response.getData().getOriginId());            
            assertEquals(origin.getName(), response.getData().getOrigin());
            assertEquals(request.getDestId(), response.getData().getDestId());
            assertEquals(destination.getName(), response.getData().getDestination());
            assertEquals(request.getTripDistance(), response.getData().getTripDistance());
            assertEquals(request.getTripDuration(), response.getData().getTripDuration());
        });
    }

    @Test
    void testRegisterRouteBlank() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        
        
        RegisterRouteRequest request = new RegisterRouteRequest();        
        request.setOriginId(null);
        request.setDestId(null);
        request.setTripDistance(null);
        request.setTripDuration(null);        

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
                post("/api/routes")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<RouteResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testRegisterRouteDuplicate() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        

        StationEntity origin = stationRepository.findByCode("GMR").orElse(null);
        StationEntity destination = stationRepository.findByCode("BD").orElse(null);

        RegisterRouteRequest request = new RegisterRouteRequest();        
        request.setOriginId(origin.getId());
        request.setDestId(destination.getId());
        request.setTripDistance(180.0);
        request.setTripDuration(3.0);        

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
                post("/api/routes")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<RouteResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testRegisterRouteInvalidToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        

        StationEntity origin = stationRepository.findByCode("BD").orElse(null);
        StationEntity destination = stationRepository.findByCode("GMR").orElse(null);

        RegisterRouteRequest request = new RegisterRouteRequest();        
        request.setOriginId(origin.getId());
        request.setDestId(destination.getId());
        request.setTripDistance(180.0);
        request.setTripDuration(3.0);        

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
                post("/api/routes")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<RouteResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testRegisterRouteTokenExpired() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        

        StationEntity origin = stationRepository.findByCode("BD").orElse(null);
        StationEntity destination = stationRepository.findByCode("GMR").orElse(null);

        RegisterRouteRequest request = new RegisterRouteRequest();        
        request.setOriginId(origin.getId());
        request.setDestId(destination.getId());
        request.setTripDistance(180.0);
        request.setTripDuration(3.0);        

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
                post("/api/routes")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<RouteResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testRegisterRouteNoToken() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        

        StationEntity origin = stationRepository.findByCode("BD").orElse(null);
        StationEntity destination = stationRepository.findByCode("GMR").orElse(null);

        RegisterRouteRequest request = new RegisterRouteRequest();        
        request.setOriginId(origin.getId());
        request.setDestId(destination.getId());
        request.setTripDistance(180.0);
        request.setTripDuration(3.0);        

        Authentication authentication = authenticationManager.authenticate(
                                            new UsernamePasswordAuthenticationToken(
                                                email, password)
                                            );

        String mockToken = jwtUtil.generateToken(authentication);

        user.setToken(mockToken);
        user.setTokenExpiredAt(System.currentTimeMillis() + securityConstants.getJwtExpiration());
        userRepository.save(user);

        mockMvc.perform(
                post("/api/routes")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))                                            
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
                WebResponse<RouteResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testRegisterRouteBadRole() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);   
        
        RoleEntity role = roleRepository.findByName("ROLE_USER").orElse(null);
        
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);

        StationEntity origin = stationRepository.findByCode("BD").orElse(null);
        StationEntity destination = stationRepository.findByCode("GMR").orElse(null);

        RegisterRouteRequest request = new RegisterRouteRequest();        
        request.setOriginId(origin.getId());
        request.setDestId(destination.getId());
        request.setTripDistance(180.0);
        request.setTripDuration(3.0);        

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
                post("/api/routes")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isForbidden()
        ).andDo(result -> {
                WebResponse<RouteResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testGetRouteSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        

        StationEntity origin = stationRepository.findByCode("GMR").orElse(null);
        StationEntity destination = stationRepository.findByCode("BD").orElse(null);       

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
                get("/api/routes/origin/" + origin.getId() + "/destination/" + destination.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<RouteResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(origin.getId(), response.getData().getOriginId());            
            assertEquals(origin.getName(), response.getData().getOrigin());
            assertEquals(destination.getId(), response.getData().getDestId());
            assertEquals(destination.getName(), response.getData().getDestination());            
        });
    }

    @Test
    void testGetRouteOriginNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        
        
        StationEntity destination = stationRepository.findByCode("BD").orElse(null);       

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
                get("/api/routes/origin/111/destination/" + destination.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
                WebResponse<RouteResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());           
        });
    }

    @Test
    void testGetRouteOriginBadId() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        
        
        StationEntity destination = stationRepository.findByCode("BD").orElse(null);       

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
                get("/api/routes/origin/EEE/destination/" + destination.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<RouteResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());           
        });
    }

    @Test
    void testGetRouteDestinationNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        

        StationEntity origin = stationRepository.findByCode("GMR").orElse(null);              

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
                get("/api/routes/origin/" + origin.getId() + "/destination/111")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
                WebResponse<RouteResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());          
        });
    }

    @Test
    void testGetRouteDestinationBadId() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        

        StationEntity origin = stationRepository.findByCode("GMR").orElse(null);              

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
                get("/api/routes/origin/" + origin.getId() + "/destination/EEE")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<RouteResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());          
        });
    }

    @Test
    void testUpdateRouteSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        

        StationEntity origin = stationRepository.findByCode("GMR").orElse(null);
        StationEntity destination = stationRepository.findByCode("BD").orElse(null);

        StationEntity newOri = stationRepository.findByCode("BD").orElse(null);
        StationEntity newDest = stationRepository.findByCode("SR").orElse(null);

        RouteEntity route = routeRepository.findByOriginAndDestination(origin, destination).orElse(null);

        UpdateRouteRequest request = new UpdateRouteRequest();        
        request.setOriginId(newOri.getId());
        request.setDestId(newDest.getId());
        request.setTripDistance(180.0);
        request.setTripDuration(7.0);        

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
                patch("/api/routes/" + route.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<RouteResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(request.getOriginId(), response.getData().getOriginId());            
            assertEquals(newOri.getName(), response.getData().getOrigin());
            assertEquals(request.getDestId(), response.getData().getDestId());
            assertEquals(newDest.getName(), response.getData().getDestination());
            assertEquals(request.getTripDistance(), response.getData().getTripDistance());
            assertEquals(request.getTripDuration(), response.getData().getTripDuration());
        });
    }

    @Test
    void testUpdateRouteDuplicate() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        

        StationEntity origin = stationRepository.findByCode("GMR").orElse(null);
        StationEntity destination = stationRepository.findByCode("BD").orElse(null);

        StationEntity newOri = stationRepository.findByCode("BD").orElse(null);
        StationEntity newDest = stationRepository.findByCode("YK").orElse(null);

        RouteEntity route = routeRepository.findByOriginAndDestination(origin, destination).orElse(null);

        UpdateRouteRequest request = new UpdateRouteRequest();        
        request.setOriginId(newOri.getId());
        request.setDestId(newDest.getId());
        request.setTripDistance(180.0);
        request.setTripDuration(7.0);        

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
                patch("/api/routes/" + route.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<RouteResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testUpdateRouteBadId() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        

        StationEntity origin = stationRepository.findByCode("GMR").orElse(null);
        StationEntity destination = stationRepository.findByCode("BD").orElse(null);

        StationEntity newOri = stationRepository.findByCode("BD").orElse(null);
        StationEntity newDest = stationRepository.findByCode("SR").orElse(null);

        RouteEntity route = routeRepository.findByOriginAndDestination(origin, destination).orElse(null);

        UpdateRouteRequest request = new UpdateRouteRequest();        
        request.setOriginId(newOri.getId());
        request.setDestId(newDest.getId());
        request.setTripDistance(180.0);
        request.setTripDuration(7.0);        

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
                patch("/api/routes/" + route.getId() + "a")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<RouteResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testUpdateRouteNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        

        StationEntity newOri = stationRepository.findByCode("BD").orElse(null);
        StationEntity newDest = stationRepository.findByCode("SR").orElse(null);        

        UpdateRouteRequest request = new UpdateRouteRequest();        
        request.setOriginId(newOri.getId());
        request.setDestId(newDest.getId());
        request.setTripDistance(180.0);
        request.setTripDuration(7.0);        

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
                patch("/api/routes/111111")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isNotFound()
        ).andDo(result -> {
                WebResponse<RouteResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });
    }

    @Test
    void testGetAllRouteSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        
       
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
                get("/api/routes")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<List<RouteResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());            
            assertEquals(10, response.getData().size());
            assertEquals(1, response.getPaging().getTotalPage());
            assertEquals(0, response.getPaging().getCurrentPage());
            assertEquals(10, response.getPaging().getSize());
        });
    }

    @Test
    void testDeleteRouteSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        

        StationEntity origin = stationRepository.findByCode("GMR").orElse(null);
        StationEntity destination = stationRepository.findByCode("BD").orElse(null);       

        RouteEntity route = routeRepository.findByOriginAndDestination(origin, destination).orElse(null);

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
                delete("/api/routes/" + route.getId())
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
    void testSearchRouteSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        
       
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
                get("/api/routes/search")
                        .queryParam("originCode", "BD")                                                                    
                        .queryParam("destCode", "SMT")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<List<RouteResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());            
            assertEquals(2, response.getData().size());
            assertEquals(1, response.getPaging().getTotalPage());
            assertEquals(0, response.getPaging().getCurrentPage());
            assertEquals(10, response.getPaging().getSize());
        });
    }

    @Test
    void testSearchRouteNotFound() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        
       
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
                get("/api/routes/search")
                        .queryParam("originCode", "EEE")                                                                    
                        .queryParam("destCode", "EEE")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)                        
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<List<RouteResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());            
            assertEquals(0, response.getData().size());
            assertEquals(0, response.getPaging().getTotalPage());
            assertEquals(0, response.getPaging().getCurrentPage());
            assertEquals(10, response.getPaging().getSize());
        });
    }
}
