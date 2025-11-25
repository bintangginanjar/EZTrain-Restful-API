package restful.api.eztrain.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.Collections;
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

import restful.api.eztrain.entity.CoachTypeEntity;
import restful.api.eztrain.entity.RoleEntity;
import restful.api.eztrain.entity.RouteEntity;
import restful.api.eztrain.entity.RoutePriceEntity;
import restful.api.eztrain.entity.StationEntity;
import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.model.RegisterRoutePriceRequest;
import restful.api.eztrain.model.RoutePriceResponse;
import restful.api.eztrain.model.WebResponse;
import restful.api.eztrain.repository.CoachTypeRepository;
import restful.api.eztrain.repository.RoleRepository;
import restful.api.eztrain.repository.RoutePriceRepository;
import restful.api.eztrain.repository.RouteRepository;
import restful.api.eztrain.repository.StationRepository;
import restful.api.eztrain.repository.UserRepository;
import restful.api.eztrain.security.JwtUtil;
import restful.api.eztrain.security.SecurityConstants;

@EnableWebMvc
@SpringBootTest
@AutoConfigureMockMvc
public class RoutePriceControllerTest {

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
    private RoutePriceRepository routePriceRepository;

    @Autowired
    private CoachTypeRepository coachTypeRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SecurityConstants securityConstants;

    @Autowired
    private ObjectMapper objectMapper;

    private final String email = "admin@gmail.com";
    private final String password = "rahasia";

    @BeforeEach
    void setUp() {             
        routePriceRepository.deleteAll();
        
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        RoleEntity role = roleRepository.findByName("ROLE_ADMIN").orElse(null);
        
        user.setRoles(Collections.singletonList(role));          
        userRepository.save(user);
    }

    @Test
    void testRegisterRoutePriceSuccess() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        

        StationEntity origin = stationRepository.findByCode("GMR").orElse(null);
        StationEntity destination = stationRepository.findByCode("BD").orElse(null);

        RouteEntity route = routeRepository.findByOriginAndDestination(origin, destination).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName("Premium").orElse(null);

        RegisterRoutePriceRequest request = new RegisterRoutePriceRequest();        
        request.setRouteId(route.getId());
        request.setCoachTypeId(coachType.getId());  
        request.setPrice(150000.0);        

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
                post("/api/routeprices")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
                WebResponse<RoutePriceResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(true, response.getStatus());
            assertEquals(request.getPrice(), response.getData().getPrice());            
            assertEquals(coachType.getId(), response.getData().getCoachTypeId());
            assertEquals(coachType.getName(), response.getData().getCoachType());
            assertEquals(route.getId(), response.getData().getRouteId());
            assertEquals(route.getOrigin().getName(), response.getData().getOrigin());
            assertEquals(route.getDestination().getName(), response.getData().getDestination());            
        });
    }

    @Test
    void testRegisterRoutePriceBlank() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        
    
        RegisterRoutePriceRequest request = new RegisterRoutePriceRequest();        
        request.setRouteId(null);
        request.setCoachTypeId(null);  
        request.setPrice(150000.0);        

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
                post("/api/routeprices")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<RoutePriceResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());           
        });
    }

    @Test
    void testRegisterRoutePriceDuplicate() throws Exception {
        UserEntity user = userRepository.findByEmail(email).orElse(null);        

        StationEntity origin = stationRepository.findByCode("GMR").orElse(null);
        StationEntity destination = stationRepository.findByCode("BD").orElse(null);

        RouteEntity route = routeRepository.findByOriginAndDestination(origin, destination).orElse(null);

        CoachTypeEntity coachType = coachTypeRepository.findByName("Premium").orElse(null);

        RoutePriceEntity routePrice = new RoutePriceEntity();
        routePrice.setPrice(150000.0);
        routePrice.setCoachTypeEntity(coachType);
        routePrice.setRouteEntity(route);
        routePrice.setUserEntity(user);
        routePriceRepository.save(routePrice);

        RegisterRoutePriceRequest request = new RegisterRoutePriceRequest();        
        request.setRouteId(route.getId());
        request.setCoachTypeId(coachType.getId());  
        request.setPrice(150000.0);        

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
                post("/api/routeprices")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", mockBearerToken)                        
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
                WebResponse<RoutePriceResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals(false, response.getStatus());
        });        
    }
}
