package restful.api.eztrain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import restful.api.eztrain.entity.RouteEntity;
import restful.api.eztrain.entity.StationEntity;
import restful.api.eztrain.entity.UserEntity;
import restful.api.eztrain.mapper.ResponseMapper;
import restful.api.eztrain.model.RegisterRouteRequest;
import restful.api.eztrain.model.RouteResponse;
import restful.api.eztrain.repository.RouteRepository;
import restful.api.eztrain.repository.StationRepository;
import restful.api.eztrain.repository.UserRepository;

@Service
public class RouteService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private ValidationService validationService;

    @Transactional
    public RouteResponse register(Authentication authentication, RegisterRouteRequest request) {
        validationService.validate(request);

        UserEntity user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Long originId;
        Long destId;

        try {
            originId = Long.parseLong(request.getStrOriginId());
            destId = Long.parseLong(request.getStrDestId());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request");
        }

        StationEntity origin = stationRepository.findById(originId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Origin station not found"));

        StationEntity destination = stationRepository.findById(destId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination station not found"));

        RouteEntity route = new RouteEntity();
        route.setOrigin(origin);
        route.setDestination(destination);
        route.setTripDistance(request.getTripDistance());
        route.setTripDuration(request.getTripDuration());
        route.setUserEntity(user);

        try {
            routeRepository.save(route);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Register route failed");
        }

        return ResponseMapper.ToRouteResponseMapper(route);
    }

}
