package restful.api.eztrain.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
import restful.api.eztrain.model.SearchRouteRequest;
import restful.api.eztrain.model.UpdateRouteRequest;
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

        StationEntity origin = stationRepository.findById(request.getOriginId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Origin station not found"));

        StationEntity destination = stationRepository.findById(request.getDestId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination station not found"));

        if (routeRepository.findByOriginAndDestination(origin, destination).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Route already registered");
        }

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

    @Transactional(readOnly = true)
    public RouteResponse get(Long originId, Long destId) {
        StationEntity origin = stationRepository.findById(originId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Origin station not found"));

        StationEntity destination = stationRepository.findById(destId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination station not found"));

        RouteEntity route = routeRepository.findByOriginAndDestination(origin, destination)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Route not found"));

        return ResponseMapper.ToRouteResponseMapper(route);
    }

    @Transactional(readOnly = true)
    public Page<RouteResponse> getAllRoutes(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<RouteEntity> routes = routeRepository.findAll(pageable);

        List<RouteResponse> routeResponses = routes
                                            .getContent()
                                            .stream()
                                            .map(p -> ResponseMapper.ToRouteResponseMapper(p))
                                            .collect(Collectors.toList()); 

        return new PageImpl<>(routeResponses, pageable, routes.getTotalElements());
    }

    @Transactional
    public RouteResponse update(Authentication authentication, UpdateRouteRequest request, Long routeId) {        
        UserEntity user = userRepository.findByEmail(authentication.getName())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        StationEntity origin = stationRepository.findById(request.getOriginId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Origin station not found"));

        StationEntity destination = stationRepository.findById(request.getDestId())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination station not found"));

        RouteEntity route = routeRepository.findById(routeId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Route not found"));

        if (Objects.nonNull(request.getOriginId())) {
            route.setOrigin(origin);
        }
        
        if (Objects.nonNull(request.getDestId())) {
            route.setDestination(destination);
        }

        if (Objects.nonNull(request.getTripDistance())) {
            route.setTripDistance(request.getTripDistance());
        }

        if (Objects.nonNull(request.getTripDuration())) {
            route.setTripDuration(request.getTripDuration());
        }

        route.setUserEntity(user);

        try {
            routeRepository.save(route);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Update route failed");
        }

        return ResponseMapper.ToRouteResponseMapper(route);
    }

    @Transactional
    public void delete(Long routeId) {
        RouteEntity route = routeRepository.findById(routeId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Route not found"));

        try {
            routeRepository.delete(route);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Delete route failed");
        }                                        
    }

    @Transactional(readOnly = true)
    public Page<RouteResponse> search(SearchRouteRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize()); 
        Optional<StationEntity> origin = stationRepository.findByCode(request.getOrigin());
        Optional<StationEntity> destination = stationRepository.findByCode(request.getDestination());

        if (origin.isEmpty() || destination.isEmpty()) {
            return Page.empty(pageable);
        }
                
        Page<RouteEntity> routes = routeRepository.findByOriginAndDestination(origin.get(), destination.get(), pageable);
        List<RouteResponse> stationResponses = routes
                                                    .getContent()
                                                    .stream()
                                                    .map(route -> ResponseMapper.ToRouteResponseMapper(route))
                                                    .collect(Collectors.toList());

        return new PageImpl<>(stationResponses, pageable, routes.getTotalElements());

    }

}
